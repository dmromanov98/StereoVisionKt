package ru.mirea.core.models

import ru.mirea.core.enums.QualityOfVideo

data class SettingsParams(
    val hsvParams: HSVParams,
    val focusLength: Double,
    val staffUpdatePeriod: Long,
    val delay: Long,
    val methodNumber: Byte,
    val distanceBetweenCameras: Double,
    val ratio: Double,
    val qualityOfVideo: QualityOfVideo,
    val measurementNumber: Int,
    val verticalAccounting: Boolean
)