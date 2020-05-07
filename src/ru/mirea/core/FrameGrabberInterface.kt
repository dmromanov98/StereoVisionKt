package ru.mirea.core

import javafx.scene.image.Image

interface FrameGrabberInterface {
    fun loadImage(image: Image, cameraId: Int)
    fun loadMaskImage(image: Image, cameraId: Int)
    fun loadMorphImage(image: Image, cameraId: Int)
    fun beforeRunning(cameraId: Int)
    fun afterRunning(cameraId: Int)
}