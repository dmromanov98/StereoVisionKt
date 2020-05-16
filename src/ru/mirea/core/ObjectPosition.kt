package ru.mirea.core

import javafx.application.Platform
import org.opencv.core.Point
import ru.mirea.core.exceptions.MethodNotFoundException
import ru.mirea.core.interfaces.CamerasProcessorInterface
import ru.mirea.core.interfaces.ObjectPositionInterface
import ru.mirea.core.models.ObjectPositionModel
import kotlin.math.*

class ObjectPosition(
    private val initializer: CamerasProcessorInterface
) : ObjectPositionInterface {

    private var centerOfVideos: Point? = null

    private var dFirstCameraDownPoint: Point? = null
    private var dFirstCameraUpperPoint: Point? = null
    private var dSecondCameraDownPoint: Point? = null
    private var dSecondCameraUpperPoint: Point? = null
    private var methodNumber: Byte = 1
    private var ratio = 26.5
    var calculating: Boolean = false

    private var firstCameraDownPoint = Point(0.0, 0.0)
    private var firstCameraUpperPoint = Point(0.0, 0.0)
    private var secondCameraDownPoint = Point(0.0, 0.0)
    private var secondCameraUpperPoint = Point(0.0, 0.0)
    private var focus = 0.0
    private var distanceBetweenCameras = 0.0
    private var timer: Timer? = null

    companion object {
        var staffUpdatePeriod: Long = 33
        var delay: Long = 0
    }

    fun reloadTimerParameters(): ObjectPosition {
        if (timer != null) {
            timer!!.stop()
            timer = null
            runTimer()
        }
        return this
    }

    private fun runTimer() {
        Timer.staffUpdatePeriod = FrameGrabber.staffUpdatePeriod
        Timer.delay = FrameGrabber.delay
        timer = Timer(this)
    }

    fun initAndRun() {
        if (!calculating && centerOfVideos != null) {
            runTimer()
        } else if (timer != null) {
            timer!!.stop()
            timer = null
        }
    }

    fun withCenterOfVideos(centerOfVideos: Point): ObjectPosition {
        this.centerOfVideos = centerOfVideos
        return this
    }


    fun withFirstCameraDownPoint(firstCameraDownPoint: Point): ObjectPosition {
        this.firstCameraDownPoint = firstCameraDownPoint
        return this
    }

    fun withFirstCameraUpperPoint(firstCameraUpperPoint: Point): ObjectPosition {
        this.firstCameraUpperPoint = firstCameraUpperPoint
        return this
    }

    fun withSecondCameraDownPoint(secondCameraDownPoint: Point): ObjectPosition {
        this.secondCameraDownPoint = secondCameraDownPoint
        return this
    }

    fun withSecondCameraUpperPoint(secondCameraUpperPoint: Point): ObjectPosition {
        this.secondCameraUpperPoint = secondCameraUpperPoint
        return this
    }

    fun withFocus(focus: Double): ObjectPosition {
        this.focus = focus
        return this
    }

    fun withDistanceBetweenCameras(distanceBetweenCameras: Double): ObjectPosition {
        this.distanceBetweenCameras = distanceBetweenCameras
        return this
    }

    fun withMethod(methodNumber: Byte): ObjectPosition {
        this.methodNumber = methodNumber
        return this
    }

    fun withRatio(ratio: Double): ObjectPosition {
        this.ratio = ratio
        return this
    }

    override fun calculateDistances() {
        deviationCount()
        val distanceDownPoint = distanceDownPoint()
        val distanceUpperPoint = distanceUpperPoint()
        val clicked = checkClicked(distanceDownPoint, distanceUpperPoint)
        val objectPosition = ObjectPositionModel(distanceDownPoint, distanceUpperPoint, clicked)
        Platform.runLater { initializer.loadObjectPosition(objectPosition) }
    }

    override fun checkClicked(distanceDownPoint: Double, distanceUpperPoint: Double): Boolean {
        return distanceUpperPoint > distanceDownPoint
    }

    override fun distanceDownPoint() = when (methodNumber) {
        1.toByte() -> calculateDistanceWithFirstMethod(
            dFirstCameraDownPoint!!,
            dSecondCameraDownPoint!!
        )
        2.toByte() -> calculateDistanceWithSecondMethod(
            dFirstCameraDownPoint!!,
            dSecondCameraDownPoint!!,
            centerOfVideos!!
        )
        else -> throw MethodNotFoundException("Method with number $methodNumber not found")
    }

    override fun distanceUpperPoint() = when (methodNumber) {
        1.toByte() -> calculateDistanceWithFirstMethod(
            dFirstCameraUpperPoint!!,
            dSecondCameraUpperPoint!!
        )
        2.toByte() -> calculateDistanceWithSecondMethod(
            dFirstCameraUpperPoint!!,
            dSecondCameraUpperPoint!!,
            centerOfVideos!!
        )
        else -> throw MethodNotFoundException("Method with number $methodNumber not found")
    }

    override fun calculateDistanceWithFirstMethod(point1: Point, point2: Point): Double {
        val alpha = 90 - Math.toDegrees(atan(point1.x / focus))
        val beta = 90 - Math.toDegrees(atan(point2.x / focus))
        val gamma = 180 - alpha - beta
        val m: Double = (sin(Math.toRadians(alpha)) * distanceBetweenCameras) / sin(Math.toRadians(gamma))
        val r = sqrt(
            (distanceBetweenCameras / 2).pow(2.0) + m.pow(2.0) -
                    distanceBetweenCameras * m * cos(Math.toRadians(beta))
        )
        val dy = (point1.y + point2.y) / 2
        return r / cos(90 - atan(dy / r))
    }

    override fun calculateDistanceWithSecondMethod(point1: Point, point2: Point, center: Point): Double {
        val alpha: Double = 90.0 - (point1.x / (center.x * ratio))
        val beta: Double = 90.0 - (point2.x / (center.x * ratio))
        val gamma = 180 - alpha - beta
        val dy = (point1.y + point2.y) / 2
        val alphaY = dy / (center.x * ratio)
        val b = (distanceBetweenCameras / sin(Math.toRadians(gamma))) * sin(Math.toRadians(beta)) /
                cos(Math.toRadians(alphaY))
        val c = (distanceBetweenCameras / sin(Math.toRadians(gamma))) * sin(Math.toRadians(alpha)) /
                cos(Math.toRadians(alphaY))
        return 0.5 * sqrt(2 * b.pow(2) + 2 * c.pow(2) - distanceBetweenCameras.pow(2))
    }

    override fun deviationCount() {
        dFirstCameraDownPoint = getDeviation(centerOfVideos!!, firstCameraDownPoint)
        dFirstCameraUpperPoint = getDeviation(centerOfVideos!!, firstCameraUpperPoint)
        dSecondCameraDownPoint = getDeviation(centerOfVideos!!, secondCameraDownPoint)
        dSecondCameraUpperPoint = getDeviation(centerOfVideos!!, secondCameraUpperPoint)
    }

    private fun getDeviation(centerOfGrabber: Point, point: Point) =
        Point(
            abs(centerOfGrabber.x - point.x),
            abs(centerOfGrabber.y - point.y)
        )

    override fun stopCalculating() {
        initAndRun()
    }

    override fun run() {
        calculateDistances()
    }
}