package ru.mirea.gui

import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import ru.mirea.core.FrameGrabber
import ru.mirea.core.FrameGrabberInterface
import ru.mirea.core.models.HSVParams
import tornadofx.*

class MainWindow : View("Detection Object Definition"), FrameGrabberInterface {
    var hsvParams = HSVParams()
    private val fitWidthImage = 620.0
    private val fitHeightImage = 620.0
    private val fitWidthMaskAndMorphImage = 365.0
    private val fitHeightMaskAndMorphImage = 365.0

    companion object {
        var leftGrabber: FrameGrabber? = null
        var rightGrabber: FrameGrabber? = null
    }

    lateinit var leftImage: ImageView
    lateinit var rightImage: ImageView
    lateinit var leftMorphImage: ImageView
    lateinit var rightMorphImage: ImageView
    lateinit var leftMaskImage: ImageView
    lateinit var rightMaskImage: ImageView
    lateinit var leftCameraId: TextField
    lateinit var rightCameraId: TextField
    lateinit var leftCameraButton: Button
    lateinit var rightCameraButton: Button

    override val root: StackPane = stackpane {

        prefWidth = 1280.0
        prefHeight = 800.0
        tabpane {
            tab("Первичная настройка камер") {
                anchorpane {
                    rightImage = imageview {
                        fitWidth = fitWidthImage
                        fitHeight = fitHeightImage
                        AnchorPane.setTopAnchor(this@imageview, 15.0)
                        AnchorPane.setRightAnchor(this@imageview, 15.0)
                    }
                    leftImage = imageview {
                        fitWidth = fitWidthImage
                        fitHeight = fitHeightImage
                        AnchorPane.setTopAnchor(this@imageview, 15.0)
                        AnchorPane.setLeftAnchor(this@imageview, 15.0)
                    }
                    leftCameraButton = button {
                        action {
                            startLeftCamera()
                        }
                        text = "Включить левую камеру"
                        AnchorPane.setBottomAnchor(this@button, 15.0)
                        AnchorPane.setLeftAnchor(this@button, 15.0)
                    }
                    leftCameraId = textfield {
                        filterInput { it.controlNewText.isInt() }
                        promptText = "Id камеры"
                        AnchorPane.setBottomAnchor(this@textfield, 40.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                    }
                    rightCameraButton = button {
                        action {
                            startRightCamera()
                        }
                        text = "Включить правую камеру"
                        AnchorPane.setBottomAnchor(this@button, 15.0)
                        AnchorPane.setRightAnchor(this@button, 15.0)
                    }
                    rightCameraId = textfield {
                        filterInput { it.controlNewText.isInt() }
                        promptText = "Id камеры"
                        AnchorPane.setBottomAnchor(this@textfield, 40.0)
                        AnchorPane.setRightAnchor(this@textfield, 15.0)
                    }
                }
            }
            tab("Настройки определения объекта") {
                anchorpane {
                    slider {
                        min = 0.0
                        max = 180.0
                        prefWidth = 330.0
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 50.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        value = hsvParams.hueStart
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(hueStart = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Стартовое значение тона") {
                        AnchorPane.setTopAnchor(this@label, 45.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)

                    }
                    slider {
                        min = 0.0
                        max = 180.0
                        prefWidth = 330.0
                        value = hsvParams.hueStop
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 100.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(hueStop = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Конечное значение тона") {
                        AnchorPane.setTopAnchor(this@label, 95.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    separator {
                        orientation = Orientation.HORIZONTAL
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@separator, 150.0)
                        AnchorPane.setLeftAnchor(this@separator, 15.0)
                    }
                    slider {
                        min = 0.0
                        max = 255.0
                        prefWidth = 330.0
                        value = hsvParams.saturationStart
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 200.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(saturationStart = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Стартовое значение насыщенности") {
                        AnchorPane.setTopAnchor(this@label, 195.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }

                    slider {
                        min = 0.0
                        max = 255.0
                        prefWidth = 330.0
                        value = hsvParams.saturationStop
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 250.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(saturationStop = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Конечное значение насыщенности") {
                        AnchorPane.setTopAnchor(this@label, 245.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    separator {
                        orientation = Orientation.HORIZONTAL
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@separator, 300.0)
                        AnchorPane.setLeftAnchor(this@separator, 15.0)
                    }
                    slider {
                        min = 0.0
                        max = 255.0
                        prefWidth = 330.0
                        value = hsvParams.valueStart
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 350.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(valueStart = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Стартовое значение") {
                        AnchorPane.setTopAnchor(this@label, 345.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    slider {
                        min = 0.0
                        max = 255.0
                        prefWidth = 330.0
                        value = hsvParams.valueStop
                        AnchorPane.setLeftAnchor(this@slider, 15.0)
                        AnchorPane.setTopAnchor(this@slider, 400.0)
                        majorTickUnit = 10.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        valueProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
                            hsvParams = hsvParams.copy(valueStop = newValue.toDouble())
                            setHSVParams()
                        }
                    }
                    label("Конечное значение") {
                        AnchorPane.setTopAnchor(this@label, 395.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    leftMaskImage = imageview {
                        fitWidth = fitWidthMaskAndMorphImage
                        fitHeight = fitHeightMaskAndMorphImage
                        AnchorPane.setTopAnchor(this@imageview, 15.0)
                        AnchorPane.setRightAnchor(this@imageview, 385.0)
                    }
                    leftMorphImage = imageview {
                        fitWidth = fitWidthMaskAndMorphImage
                        fitHeight = fitHeightMaskAndMorphImage
                        AnchorPane.setTopAnchor(this@imageview, 15.0)
                        AnchorPane.setRightAnchor(this@imageview, 15.0)
                    }
                    rightMaskImage = imageview {
                        fitWidth = fitWidthMaskAndMorphImage
                        fitHeight = fitHeightMaskAndMorphImage
                        AnchorPane.setBottomAnchor(this@imageview, 15.0)
                        AnchorPane.setRightAnchor(this@imageview, 385.0)
                    }
                    rightMorphImage = imageview {
                        fitWidth = fitWidthMaskAndMorphImage
                        fitHeight = fitHeightMaskAndMorphImage
                        AnchorPane.setBottomAnchor(this@imageview, 15.0)
                        AnchorPane.setRightAnchor(this@imageview, 15.0)
                    }
                    label("Левая камера: ") {
                        layoutX = 371.0
                        layoutY = 15.0
                    }
                    label("Правая камера") {
                        layoutX = 371.0
                        layoutY = 380.0
                    }
                }
            }
        }
    }

    private fun startLeftCamera() {
        if ((leftGrabber == null || !leftGrabber!!.isThreadRun) && leftCameraId.text.isNotEmpty()) {
            leftGrabber = FrameGrabber(
                leftCameraId.text.toInt(),
                fitWidthImage.toInt(),
                fitHeightImage.toInt(),
                this
            ).withBlur()
            leftGrabber!!.initAndRunOrStop()
            setHSVParams()
        } else if ((leftGrabber != null && leftGrabber!!.isThreadRun) && leftCameraId.text.isNotEmpty()) {
            leftGrabber!!.initAndRunOrStop()
            leftGrabber = null
        }
    }

    private fun startRightCamera() {
        if ((rightGrabber == null || !rightGrabber!!.isThreadRun) && rightCameraId.text.isNotEmpty()) {
            rightGrabber = FrameGrabber(
                rightCameraId.text.toInt(),
                fitWidthImage.toInt(),
                fitHeightImage.toInt(),
                this
            ).withBlur()
            rightGrabber!!.initAndRunOrStop()
            setHSVParams()
        } else if ((rightGrabber != null && rightGrabber!!.isThreadRun) && rightCameraId.text.isNotEmpty()) {
            rightGrabber!!.initAndRunOrStop()
            rightGrabber = null
        }
    }

    override fun loadImage(image: Image, cameraId: Int) {
        if (cameraId == leftCameraId.text.toIntOrNull()) {
            leftImage.image = image
        } else if (cameraId == rightCameraId.text.toIntOrNull()) {
            rightImage.image = image
        }
    }

    override fun loadMaskImage(image: Image, cameraId: Int) {
        if (cameraId == leftCameraId.text.toIntOrNull()) {
            leftMaskImage.image = image
        } else if (cameraId == rightCameraId.text.toIntOrNull()) {
            rightMaskImage.image = image
        }
    }

    override fun loadMorphImage(image: Image, cameraId: Int) {
        if (cameraId == leftCameraId.text.toIntOrNull()) {
            leftMorphImage.image = image
        } else if (cameraId == rightCameraId.text.toIntOrNull()) {
            rightMorphImage.image = image
        }
    }

    override fun beforeRunning(cameraId: Int) {
        if (cameraId == leftCameraId.text.toIntOrNull()) {
            leftCameraId.isEditable = false
            leftCameraButton.text = "Выключить левую камеру"
        } else if (cameraId == rightCameraId.text.toIntOrNull()) {
            rightCameraId.isEditable = false
            rightCameraButton.text = "Выключить правую камеру"
            rightGrabber
        }
    }

    override fun afterRunning(cameraId: Int) {
        if (cameraId == leftCameraId.text.toIntOrNull()) {
            leftCameraId.isEditable = true
            leftCameraButton.text = "Включить левую камеру"
        } else if (cameraId == rightCameraId.text.toIntOrNull()) {
            rightCameraId.isEditable = true
            rightCameraButton.text = "Включить правую камеру"
        }
    }

    private fun setHSVParams() {
        if (leftGrabber != null) {
            leftGrabber!!.withHSVRange(hsvParams)
        }
        if (rightGrabber != null) {
            rightGrabber!!.withHSVRange(hsvParams)
        }
    }
}