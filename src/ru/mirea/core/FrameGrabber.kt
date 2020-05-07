package ru.mirea.core

import javafx.embed.swing.SwingFXUtils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import ru.mirea.core.models.HSVParams
import ru.mirea.gui.MainWindow
import tornadofx.runLater
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class FrameGrabber(
    private val cameraId: Int,
    private val width: Int,
    private val height: Int,
    private val initializer: MainWindow
) : Runnable {

    lateinit var capture: VideoCapture
    var timer: ScheduledExecutorService? = null
    var mainFrameImage = Mat()
    var hsvMaskFrameImage = Mat()
    var morphFrameImage = Mat()
    var blur = false
    var hsvParams = HSVParams()
    private var staffUpdatePeriod = 33
    var isThreadRun = false
    private var cameraActive = false

    fun initAndRunOrStop() {
        if (!cameraActive) {
            capture = VideoCapture()
            capture.open(cameraId)
            if (capture.isOpened) {
                runLater { initializer.beforeRunning(cameraId) }
                cameraActive = true
                run()
                timer = Executors.newSingleThreadScheduledExecutor()
                timer!!.scheduleAtFixedRate(
                    this,
                    0,
                    staffUpdatePeriod.toLong(),
                    TimeUnit.MILLISECONDS
                )
            }
        } else {
            cameraActive = false
            stopAcquisition()
        }
    }

    private fun stopAcquisition() {
        runLater { initializer.afterRunning(cameraId) }
        if (timer != null && !timer!!.isShutdown) {
            try {
                timer!!.shutdown()
                timer!!.awaitTermination(33, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        if (capture.isOpened) {
            capture.release()
        }
    }

    private fun removeNoise(image: Mat): Mat {
        val blurredImage = Mat()
        Imgproc.blur(image, blurredImage, Size(5.0, 5.0))
        return blurredImage
    }

    private fun convertToHSV(image: Mat): Mat {
        val hsvImage = Mat()
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV)
        return hsvImage
    }

    fun withBlur(): FrameGrabber {
        blur = true
        return this
    }

    private fun getMainFrame(): Mat {
        var frame = Mat()
        if (capture.isOpened) {
            try {
                capture.read(frame)
                if (!frame.empty()) {
                    val imageToShow = Utils.mat2Image(frame)
                    val scaledImage = Utils.getScaledImage(imageToShow, width, height)
                    frame = Utils.bufferedImage2MatV2(SwingFXUtils.fromFXImage(scaledImage, null))!!

                    if (blur) {
                        frame = removeNoise(frame)
                    }
                }
            } catch (e: Exception) {
                System.err.println(e)
            }
        }
        return frame
    }

    fun withHSVRange(hsvParams: HSVParams): FrameGrabber {
        this.hsvParams = hsvParams
        return this
    }

    private fun getFrameWithHSVRange(image: Mat): Mat {
        val frame = Mat()
        with(hsvParams) {
            val minValues = Scalar(hueStart, saturationStart, valueStart)
            val maxValues = Scalar(hueStop, saturationStop, valueStop)
            Core.inRange(image, minValues, maxValues, frame)
        }
        return frame
    }

    private fun getMorphFrame(image: Mat): Mat {
        val morph = Mat()
        val dilateElement = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(24.0, 24.0)
        )
        val erodeElement = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(12.0, 12.0)
        )

        //morphological operators
        //dilate with large element, erode with small ones
        Imgproc.erode(image, morph, erodeElement)
        Imgproc.erode(morph, morph, erodeElement)
        Imgproc.dilate(morph, morph, dilateElement)
        Imgproc.dilate(morph, morph, dilateElement)
        return morph
    }

    private fun framesProcessor() {
        try {
            mainFrameImage = getMainFrame()
            hsvMaskFrameImage = getFrameWithHSVRange(convertToHSV(mainFrameImage))
            morphFrameImage = getMorphFrame(hsvMaskFrameImage)
            mainFrameImage = findAndDrawContours(morphFrameImage, mainFrameImage)
            val centerOfObject = getObjectCenterPoint(hsvMaskFrameImage)
            Imgproc.circle(
                mainFrameImage, centerOfObject, 7,
                Scalar(255.0, 255.0, 255.0), -1
            )
            toInitializer()
        } catch (ex: java.lang.Exception) {
            isThreadRun = false
        }


    }

    private fun toInitializer() {
        runLater {
            initializer.loadImage(Utils.mat2Image(mainFrameImage)!!, cameraId)
            initializer.loadMaskImage(Utils.mat2Image(hsvMaskFrameImage)!!, cameraId)
            initializer.loadMorphImage(Utils.mat2Image(morphFrameImage)!!, cameraId)
        }
    }

    private fun getObjectCenterPoint(image: Mat): Point {
        val moments = Imgproc.moments(image)
        return Point((moments.m10 / moments.m00), (moments.m01 / moments.m00))
    }

    private fun findAndDrawContours(image: Mat, mainImage: Mat): Mat {
        val contours: List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        // find contours
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE)

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            // for each contour, display it in blue
            var idx = 0
            while (idx >= 0) {
                Imgproc.drawContours(mainImage, contours, idx, Scalar(250.0, 0.0, 0.0))
                idx = hierarchy[0, idx][0].toInt()
            }
        }
        return mainImage
    }

    override fun run() {
        framesProcessor()
        isThreadRun = true
    }
}