package ru.mirea.gui

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import org.opencv.core.Point
import ru.mirea.core.CamerasProcessor
import ru.mirea.core.enums.QualityOfVideo
import ru.mirea.core.interfaces.ObjectPositionLibraryInterface
import ru.mirea.core.models.HSVParams
import ru.mirea.core.models.ObjectPositionModel
import ru.mirea.core.models.SettingsParams
import ru.mirea.database.provider.HibernateProvider
import ru.mirea.database.schema.Settings
import tornadofx.*

class MainWindow : View("Определение позиции объекта"),
    ObjectPositionLibraryInterface {
    private var hsvParams = HSVParams()
    private var hibernateProvider = HibernateProvider().setupSession()

    private val fitWidthImage = 620.0
    private val fitHeightImage = 620.0
    private val fitWidthMaskAndMorphImage = 365.0
    private val fitHeightMaskAndMorphImage = 365.0

    private lateinit var leftImage: ImageView
    private lateinit var rightImage: ImageView
    private lateinit var leftMorphImage: ImageView
    private lateinit var rightMorphImage: ImageView
    private lateinit var leftMaskImage: ImageView
    private lateinit var rightMaskImage: ImageView
    private lateinit var leftCameraId: TextField
    private lateinit var rightCameraId: TextField
    private lateinit var macrosName: TextField
    private lateinit var leftCameraButton: Button
    private lateinit var rightCameraButton: Button
    private lateinit var hueStart: Slider
    private lateinit var hueStop: Slider
    private lateinit var saturationStart: Slider
    private lateinit var saturationStop: Slider
    private lateinit var valueStart: Slider
    private lateinit var valueStop: Slider
    private lateinit var distanceDownPointFirstTab: Label
    private lateinit var distanceUpperPointFirstTab: Label
    private lateinit var clickedFirstTab: Label
    private lateinit var qualityOfVideoComboBox: ComboBox<String>
    private lateinit var macrosTableView: TableView<Settings>
    private lateinit var focusLengthField: TextField
    private lateinit var staffUpdatePeriodField: TextField
    private lateinit var delayField: TextField
    private lateinit var methodNumberField: TextField
    private lateinit var distanceBetweenCamerasField: TextField
    private lateinit var ratioField: TextField
    private lateinit var measurementNumberField: TextField
    private lateinit var verticalAccountingCheckBox: CheckBox

    private var camerasProcessor = CamerasProcessor(this)
    private var settingsData = FXCollections.observableArrayList(hibernateProvider.get(Settings()))

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
                            initLeftCamera()
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
                            initRightCamera()
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
                    distanceUpperPointFirstTab = label {
                        AnchorPane.setBottomAnchor(this@label, 15.0)
                        AnchorPane.setLeftAnchor(this@label, 570.0)
                    }
                    distanceDownPointFirstTab = label {
                        AnchorPane.setBottomAnchor(this@label, 30.0)
                        AnchorPane.setLeftAnchor(this@label, 570.0)
                    }
                    clickedFirstTab = label {
                        AnchorPane.setBottomAnchor(this@label, 45.0)
                        AnchorPane.setLeftAnchor(this@label, 570.0)
                    }
                }
            }
            tab("Настройки определения объекта") {
                anchorpane {
                    hueStart = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
                        }
                    }
                    label("Стартовое значение тона") {
                        AnchorPane.setTopAnchor(this@label, 45.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)

                    }
                    hueStop = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
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
                    saturationStart = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
                        }
                    }
                    label("Стартовое значение \nнасыщенности") {
                        AnchorPane.setTopAnchor(this@label, 195.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    saturationStop = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
                        }
                    }
                    label("Конечное значение \nнасыщенности") {
                        AnchorPane.setTopAnchor(this@label, 245.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    separator {
                        orientation = Orientation.HORIZONTAL
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@separator, 300.0)
                        AnchorPane.setLeftAnchor(this@separator, 15.0)
                    }
                    valueStart = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
                        }
                    }
                    label("Стартовое значение") {
                        AnchorPane.setTopAnchor(this@label, 345.0)
                        AnchorPane.setLeftAnchor(this@label, 350.0)
                    }
                    valueStop = slider {
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
                            camerasProcessor.updateHSVParams(hsvParams)
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
                        layoutX = 520.0
                        layoutY = 0.0
                    }
                    label("Правая камера: ") {
                        layoutX = 520.0
                        layoutY = 375.0
                    }
                    separator {
                        orientation = Orientation.HORIZONTAL
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@separator, 450.0)
                        AnchorPane.setLeftAnchor(this@separator, 15.0)
                    }
                    focusLengthField = textfield {
                        promptText = "Фокусное расстояние"
                        filterInput { it.controlNewText.isDouble() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 480.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setFocus(newValue.toDouble())
                            }
                        }
                    }
                    staffUpdatePeriodField = textfield {
                        promptText = "Время обновления кадра (мс)"
                        filterInput { it.controlNewText.isLong() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 510.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setStaffUpdatePeriod(newValue.toLong())
                            }
                        }
                    }
                    delayField = textfield {
                        promptText = "Время задержки (мс)"
                        filterInput { it.controlNewText.isLong() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 540.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setDelay(newValue.toLong())
                            }
                        }
                    }
                    methodNumberField = textfield {
                        promptText = "Номер алгоритма (0-255)"
                        filterInput { it.controlNewText.isInt() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 570.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setMethodNumber(newValue.toByte())
                            }
                        }
                    }
                    distanceBetweenCamerasField = textfield {
                        promptText = "Расстояние между камерами"
                        filterInput { it.controlNewText.isDouble() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 600.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setDistanceBetweenCameras(newValue.toDouble())
                            }
                        }
                    }
                    ratioField = textfield {
                        promptText = "Зависимость расстояния от пикселей (для алгоритма 2)"
                        filterInput { it.controlNewText.isDouble() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 630.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setRatio(newValue.toDouble())
                            }
                        }
                    }
                    measurementNumberField = textfield {
                        promptText = "Количество измерений"
                        filterInput { it.controlNewText.isInt() }
                        prefWidth = 330.0
                        AnchorPane.setTopAnchor(this@textfield, 660.0)
                        AnchorPane.setLeftAnchor(this@textfield, 15.0)
                        textProperty().addListener { _: ObservableValue<out String>, _: String, newValue: String ->
                            if (newValue.isNotEmpty()) {
                                setMeasurementNumber(newValue.toInt())
                            }
                        }
                    }
                    qualityOfVideoComboBox = combobox(
                        SimpleStringProperty(QualityOfVideo.HIGHEST.name),
                        QualityOfVideo.values().map { it.name }) {
                        AnchorPane.setTopAnchor(this@combobox, 690.0)
                        AnchorPane.setLeftAnchor(this@combobox, 15.0)
                        valueProperty().addListener { _: ObservableValue<out String>, _: String, _: String ->
                            setQualityOfVideo()
                        }
                    }
                    verticalAccountingCheckBox = checkbox("Учитывать расстояние по вертикали при расчете") {
                        AnchorPane.setTopAnchor(this@checkbox, 720.0)
                        AnchorPane.setLeftAnchor(this@checkbox, 15.0)
                        selectedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
                            setVerticalAccounting(newValue)
                        }
                    }
                }
            }
            tab("Макросы") {
                anchorpane {
                    label {
                        text =
                            "В данном разделе вы можете сохранить текущие параметры работы."
                        AnchorPane.setTopAnchor(this@label, 15.0)
                        AnchorPane.setLeftAnchor(this@label, 15.0)
                    }
                    macrosTableView = tableview(settingsData) {
                        readonlyColumn("Идентификатор", Settings::id).fixedWidth(100)
                        readonlyColumn("Название", Settings::name).fixedWidth(300)
                        AnchorPane.setTopAnchor(this@tableview, 40.0)
                        AnchorPane.setLeftAnchor(this@tableview, 15.0)
                        prefHeight = 600.0
                        prefWidth = 405.0
                    }
                    button("Удалить выбранные параметры") {
                        prefWidth = 250.0
                        AnchorPane.setTopAnchor(this@button, 50.0)
                        AnchorPane.setLeftAnchor(this@button, 440.0)
                        action {
                            deleteSettings()
                        }
                    }
                    button("Использовать выбранные параметры") {
                        prefWidth = 250.0
                        AnchorPane.setTopAnchor(this@button, 80.0)
                        AnchorPane.setLeftAnchor(this@button, 440.0)
                        action {
                            loadSettings()
                        }
                    }
                    macrosName = textfield {
                        prefWidth = 250.0
                        promptText = "Название макроса текущих параметров"
                        AnchorPane.setTopAnchor(this@textfield, 110.0)
                        AnchorPane.setLeftAnchor(this@textfield, 440.0)
                    }
                    button("Сохранить текущие параметры") {
                        prefWidth = 250.0
                        AnchorPane.setTopAnchor(this@button, 140.0)
                        AnchorPane.setLeftAnchor(this@button, 440.0)
                        action {
                            saveSettings()
                        }
                    }
                }
            }
        }
    }

    private fun setVerticalAccounting(value: Boolean) {
        camerasProcessor.withVerticalAccounting(value)
    }

    private fun deleteSettings() {
        val selectedSettings = macrosTableView.selectedItem
        if (selectedSettings != null && hibernateProvider.delete(selectedSettings)) {
            settingsData.remove(selectedSettings)
        }
    }

    private fun loadSettings() {
        val selectedSettings = macrosTableView.selectedItem
        if (selectedSettings != null) {
            hueStart.value = selectedSettings.hueStart
            hueStop.value = selectedSettings.hueStop
            saturationStart.value = selectedSettings.saturationStart
            saturationStop.value = selectedSettings.saturationStop
            valueStart.value = selectedSettings.valueStart
            valueStop.value = selectedSettings.valueStop
            focusLengthField.text = selectedSettings.focusLength.toString()
            staffUpdatePeriodField.text = selectedSettings.staffUpdatePeriod.toString()
            delayField.text = selectedSettings.delay.toString()
            methodNumberField.text = selectedSettings.methodNumber.toString()
            distanceBetweenCamerasField.text = selectedSettings.distanceBetweenCameras.toString()
            ratioField.text = selectedSettings.ratio.toString()
            qualityOfVideoComboBox.selectionModel.select(selectedSettings.qualityOfVideo)
            measurementNumberField.text = selectedSettings.measurementNumber.toString()
            verticalAccountingCheckBox.isSelected = selectedSettings.verticalAccounting
        }
    }

    private fun saveSettings() {
        val settingsParams = camerasProcessor.getSettingsParams()
        val settings = settingsParamToSettings(settingsParams, macrosName.text)
        if (hibernateProvider.add(settings)) {
            settingsData.add(settings)
        }
    }

    private fun settingsParamToSettings(settingsParams: SettingsParams, name: String) = Settings(
        name = name,
        hueStart = settingsParams.hsvParams.hueStart,
        hueStop = settingsParams.hsvParams.hueStop,
        saturationStart = settingsParams.hsvParams.saturationStart,
        saturationStop = settingsParams.hsvParams.saturationStop,
        valueStart = settingsParams.hsvParams.valueStart,
        valueStop = settingsParams.hsvParams.valueStop,
        focusLength = settingsParams.focusLength,
        staffUpdatePeriod = settingsParams.staffUpdatePeriod,
        delay = settingsParams.delay,
        methodNumber = settingsParams.methodNumber,
        distanceBetweenCameras = settingsParams.distanceBetweenCameras,
        ratio = settingsParams.ratio,
        qualityOfVideo = settingsParams.qualityOfVideo.name,
        measurementNumber = settingsParams.measurementNumber,
        verticalAccounting = settingsParams.verticalAccounting
    )

    private fun setQualityOfVideo() {
        camerasProcessor.withQualityOfVideo(QualityOfVideo.valueOf(qualityOfVideoComboBox.value))
    }

    private fun setDelay(delay: Long) {
        camerasProcessor.withDelay(delay)
    }

    private fun setRatio(ratio: Double) {
        camerasProcessor.withRatio(ratio)
    }

    private fun setMeasurementNumber(measurementNumber: Int) {
        camerasProcessor.withMeasurementNumber(measurementNumber)
    }

    private fun setStaffUpdatePeriod(staffUpdatePeriod: Long) {
        camerasProcessor.withStaffUpdatePeriod(staffUpdatePeriod)
    }

    private fun setFocus(focus: Double) {
        camerasProcessor.withFocus(focus)
    }

    private fun setMethodNumber(methodNumber: Byte) {
        camerasProcessor.withMethod(methodNumber)
    }

    private fun setDistanceBetweenCameras(distance: Double) {
        camerasProcessor.withDistanceBetweenCameras(distance)
    }

    private fun initLeftCamera() {
        if (leftCameraId.text.isNotEmpty()) {
            camerasProcessor
                .withQualityOfVideo(QualityOfVideo.valueOf(qualityOfVideoComboBox.value))
                .startFirstCamera(
                    leftCameraId.text.toInt(),
                    hsvParams
                )
        }
    }

    private fun initRightCamera() {
        if (rightCameraId.text.isNotEmpty()) {
            camerasProcessor
                .withQualityOfVideo(QualityOfVideo.valueOf(qualityOfVideoComboBox.value))
                .startSecondCamera(
                    rightCameraId.text.toInt(),
                    hsvParams
                )
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

    override fun loadCenters(center1: Point, center2: Point) {

    }

    override fun loadObjectPosition(position: ObjectPositionModel) {
        distanceDownPointFirstTab.text = "Расстояние до нижней точки = ${position.distanceDownPoint}"
        distanceUpperPointFirstTab.text = "Расстояние до верхней точки = ${position.distanceUpperPoint}"
        clickedFirstTab.text = "Нажатие: ${if (position.clicked) "было" else "не было"}"
    }
}