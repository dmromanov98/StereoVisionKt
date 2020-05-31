package ru.mirea.core.models

data class HSVParams(
    val hueStart: Double = 0.0,
    val hueStop: Double = 180.0,
    val saturationStart: Double = 0.0,
    val saturationStop: Double = 255.0,
    val valueStart: Double = 0.0,
    val valueStop: Double = 255.0
)