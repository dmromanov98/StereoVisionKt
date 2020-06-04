package ru.mirea.core

import javafx.embed.swing.SwingFXUtils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import ru.mirea.core.models.HSVParams
import tornadofx.runLater
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.collections.ArrayList

class FrameGrabber(
    val cameraId: Int,
    var width: Int,
    var height: Int,
    private val initializer: CamerasProcessor
) : Runnable {

    private lateinit var capture: VideoCapture
    private var timer: Timer? = null
    private var mainFrameImage = Mat()
    private var hsvMaskFrameImage = Mat()
    private var morphFrameImage = Mat()
    private var blur = false
    var isThreadRun = false
    private var cameraActive = false

    companion object {
        var staffUpdatePeriod: Long = 33
        var delay: Long = 0
        var hsvParams = HSVParams()
    }

    fun reloadTimerParameters(): FrameGrabber {
        if (timer != null) {
            timer!!.stop()
            timer = null
            runTimer()
        }
        return this
    }

    private fun runTimer() {
        Timer.staffUpdatePeriod = staffUpdatePeriod
        Timer.delay = delay
        timer = Timer(this)
    }

    fun initAndRunOrStop() {
        if (!cameraActive) {
            capture = VideoCapture()
            capture.open(cameraId)
            if (capture.isOpened) {
                runLater { initializer.beforeRunning(cameraId) }
                cameraActive = true
                runTimer()
            }
        } else {
            cameraActive = false
            stopAcquisition()
        }
    }

    private fun stopAcquisition() {
        runLater { initializer.afterRunning(cameraId) }
        if (timer != null) {
            timer?.stop()
            timer = null
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
            Size(4.0, 4.0)
        )
        val erodeElement = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(2.0, 2.0)
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
            val contours = findContours(morphFrameImage)
            mainFrameImage = drawContours(morphFrameImage, mainFrameImage, contours)
            val centers = arrayListOf<Point>()
            var contoursSize = 2
            contours.forEach {
                if(contoursSize > 0) {
                    val centerOfObject = getObjectCenterPoint(it)
                    centers.add(centerOfObject)
                    Imgproc.circle(
                        mainFrameImage, centerOfObject, 5,
                        Scalar(255.0, 255.0, 255.0), -1
                    )
                    contoursSize--
                }
            }

            toInitializer(centers)
        } catch (ex: java.lang.Exception) {
            isThreadRun = false
        }


    }

    private fun toInitializer(centers: ArrayList<Point>) {
        runLater {
            initializer.loadImage(Utils.mat2Image(mainFrameImage)!!, cameraId)
            initializer.loadMaskImage(Utils.mat2Image(hsvMaskFrameImage)!!, cameraId)
            initializer.loadMorphImage(Utils.mat2Image(morphFrameImage)!!, cameraId)
            try {
                initializer.loadCenters(centers[0], centers[1], cameraId)
            } catch (ex: IndexOutOfBoundsException) {
            }
        }
    }

    private fun getObjectCenterPoint(image: Mat): Point {
        val moments = Imgproc.moments(image)
        return Point((moments.m10 / moments.m00), (moments.m01 / moments.m00))
    }

    private fun findContours(image: Mat): List<MatOfPoint> {
        val contours: List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        return contours
    }

    private fun drawContours(image: Mat, mainImage: Mat, contours: List<MatOfPoint>): Mat {
        val hierarchy = Mat()
        // find contours
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
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