package ru.mirea.core

import javafx.scene.image.Image
import org.opencv.core.Point
import ru.mirea.core.interfaces.ObjectPositionLibraryInterface
import ru.mirea.core.interfaces.CamerasProcessorInterface
import ru.mirea.core.models.HSVParams
import ru.mirea.core.models.ObjectPositionModel

class CamerasProcessor(private val initializer: ObjectPositionLibraryInterface) : CamerasProcessorInterface {
    companion object {
        var firstCamera: FrameGrabber? = null
        var secondCamera: FrameGrabber? = null
        var objectPosition: ObjectPosition? = null
    }

    private var firstCameraId: Int? = null
    private var secondCameraId: Int? = null
    private var firstCameraFirstCenter: Point = Point(0.0, 0.0) //down point
    private var firstCameraSecondCenter: Point = Point(0.0, 0.0)  //upper point
    private var secondCameraFirstCenter: Point = Point(0.0, 0.0)  //down point
    private var secondCameraSecondCenter: Point = Point(0.0, 0.0)  //upper point
    private var distanceBetweenCameras = 130.0
    private var focus = 150.0
    private var method: Byte = 1
    private var ratio = 26.5

    fun withStaffUpdatePeriod(staffUpdatePeriod: Long): CamerasProcessor {
        FrameGrabber.staffUpdatePeriod = staffUpdatePeriod
        return this
    }

    fun withDelay(delay: Long): CamerasProcessor {
        FrameGrabber.delay = delay
        return this
    }

    fun startFirstCamera(cameraId: Int, fitWidthImage: Int, fitHeightImage: Int, hsvParams: HSVParams) {
        firstCameraId = cameraId
        if ((firstCamera == null || !firstCamera!!.isThreadRun) && cameraId != secondCamera?.cameraId
        ) {
            firstCamera = FrameGrabber(
                cameraId,
                fitWidthImage,
                fitHeightImage,
                this
            ).withBlur()
            firstCamera!!.initAndRunOrStop()
            updateHSVParams(hsvParams)
        } else if ((firstCamera != null && firstCamera!!.isThreadRun) && cameraId != secondCamera?.cameraId) {
            firstCamera!!.initAndRunOrStop()
            firstCamera = null
        }
    }

    fun startSecondCamera(cameraId: Int, fitWidthImage: Int, fitHeightImage: Int, hsvParams: HSVParams) {
        secondCameraId = cameraId
        if ((secondCamera == null || !secondCamera!!.isThreadRun) && cameraId != firstCamera?.cameraId
        ) {
            secondCamera = FrameGrabber(
                cameraId,
                fitWidthImage,
                fitHeightImage,
                this
            ).withBlur()
            secondCamera!!.initAndRunOrStop()
            updateHSVParams(hsvParams)
        } else if ((secondCamera != null && secondCamera!!.isThreadRun) && cameraId != firstCamera?.cameraId) {
            secondCamera!!.initAndRunOrStop()
            secondCamera = null
        }
    }

    fun withFocus(focus: Double): CamerasProcessor {
        this.focus = focus
        return this
    }

    fun withMethod(method: Byte): CamerasProcessor {
        this.method = method
        return this
    }

    fun withRatio(ratio: Double): CamerasProcessor {
        this.ratio = ratio
        return this
    }

    fun withDistanceBetweenCameras(distanceBetweenCameras: Double): CamerasProcessor {
        this.distanceBetweenCameras = distanceBetweenCameras
        return this
    }


    fun updateHSVParams(hsvParams: HSVParams) {
        if (firstCamera != null) {
            firstCamera!!.withHSVRange(hsvParams)
        }
        if (secondCamera != null) {
            secondCamera!!.withHSVRange(hsvParams)
        }
    }

    override fun loadImage(image: Image, cameraId: Int) {
        initializer.loadImage(image, cameraId)
    }

    override fun loadMaskImage(image: Image, cameraId: Int) {
        initializer.loadMaskImage(image, cameraId)
    }

    override fun loadMorphImage(image: Image, cameraId: Int) {
        initializer.loadMorphImage(image, cameraId)
    }

    override fun beforeRunning(cameraId: Int) {
        initializer.beforeRunning(cameraId)
    }

    override fun afterRunning(cameraId: Int) {
        initializer.afterRunning(cameraId)
    }

    override fun loadCenters(center1: Point, center2: Point, cameraId: Int) {
        if (cameraId == firstCameraId) {
            firstCameraFirstCenter = center1
            firstCameraSecondCenter = center2
        } else if (cameraId == secondCameraId) {
            secondCameraFirstCenter = center1
            secondCameraSecondCenter = center2
        }
        processCenters()
        if (firstCamera != null && secondCamera != null
            && firstCamera!!.isThreadRun && secondCamera!!.isThreadRun
        ) {
            if (objectPosition == null) {
                val firstGrabberCenter = getCenter(firstCamera!!)
                val secondGrabberCenter = getCenter(secondCamera!!)
                objectPosition = ObjectPosition(
                    firstGrabberCenter,
                    secondGrabberCenter
                    , this
                ).withDistanceBetweenCameras(distanceBetweenCameras)
                    .withFocus(focus)
                    .withFirstCameraDownPoint(firstCameraFirstCenter)
                    .withFirstCameraUpperPoint(firstCameraSecondCenter)
                    .withSecondCameraDownPoint(secondCameraFirstCenter)
                    .withSecondCameraUpperPoint(secondCameraSecondCenter)
                    .withMethod(method)
                    .withRatio(ratio)
                objectPosition!!.initAndRun()
            } else {
                objectPosition!!.withDistanceBetweenCameras(distanceBetweenCameras)
                    .withFocus(focus)
                    .withFirstCameraDownPoint(firstCameraFirstCenter)
                    .withFirstCameraUpperPoint(firstCameraSecondCenter)
                    .withSecondCameraDownPoint(secondCameraFirstCenter)
                    .withSecondCameraUpperPoint(secondCameraSecondCenter)
                    .withMethod(method)
                    .withRatio(ratio)
            }
        } else if (objectPosition != null && objectPosition!!.calculating) {
            objectPosition!!.stopCalculating()
            objectPosition = null
        }
    }

    private fun getCenter(grabber: FrameGrabber) =
        Point((grabber.width / 2).toDouble(), (grabber.height / 2).toDouble())

    override fun processCenters() {
        if (firstCameraFirstCenter.y > firstCameraSecondCenter.y
        ) {
            val tmp = firstCameraFirstCenter
            firstCameraFirstCenter = firstCameraSecondCenter
            firstCameraSecondCenter = tmp
        }
        if (secondCameraFirstCenter.y > secondCameraSecondCenter.y
        ) {
            val tmp = secondCameraFirstCenter
            secondCameraFirstCenter = secondCameraSecondCenter
            secondCameraSecondCenter = tmp
        }
    }

    override fun loadObjectPosition(position: ObjectPositionModel) {
        initializer.loadObjectPosition(position)
    }

}