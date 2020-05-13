package ru.mirea.core.interfaces

import javafx.scene.image.Image
import org.opencv.core.Point
import ru.mirea.core.models.ObjectPositionModel

interface ObjectPositionLibraryInterface {
    fun loadImage(image: Image, cameraId: Int)
    fun loadMaskImage(image: Image, cameraId: Int)
    fun loadMorphImage(image: Image, cameraId: Int)
    fun beforeRunning(cameraId: Int)
    fun afterRunning(cameraId: Int)
    fun loadCenters(center1: Point, center2: Point)
    fun loadObjectPosition(position: ObjectPositionModel)
}