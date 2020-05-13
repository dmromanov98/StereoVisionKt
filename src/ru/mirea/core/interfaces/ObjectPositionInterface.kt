package ru.mirea.core.interfaces

import org.opencv.core.Point

interface ObjectPositionInterface : Runnable {
    fun calculateDistanceWithFirstMethod(point1: Point, point2: Point): Double
    fun calculateDistanceWithSecondMethod(point1: Point, point2: Point, center: Point): Double
    fun deviationCount()
    fun calculateDistances()
    fun distanceDownPoint(): Double
    fun distanceUpperPoint(): Double
    fun checkClicked(distanceDownPoint: Double, distanceUpperPoint: Double): Boolean
    fun stopCalculating()
}