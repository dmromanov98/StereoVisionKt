package ru.mirea.database.schema

import javax.persistence.*

@Entity
@Table(name = "core_settings", schema = "public", catalog = "stereoVision")
data class Settings(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Int = 0,

    @Basic
    @Column(name = "name", nullable = false, unique = true)
    val name: String = "",

    @Basic
    @Column(name = "hue_start", nullable = false)
    val hueStart: Double = 0.0,

    @Basic
    @Column(name = "hue_stop", nullable = false)
    val hueStop: Double = 180.0,

    @Basic
    @Column(name = "saturation_start", nullable = false)
    val saturationStart: Double = 0.0,

    @Basic
    @Column(name = "saturation_stop", nullable = false)
    val saturationStop: Double = 255.0,

    @Basic
    @Column(name = "value_start", nullable = false)
    val valueStart: Double = 0.0,

    @Basic
    @Column(name = "value_stop", nullable = false)
    val valueStop: Double = 255.0,

    @Basic
    @Column(name = "focus_length", nullable = false)
    val focusLength: Double = 0.0,

    @Basic
    @Column(name = "staff_update_period", nullable = false)
    val staffUpdatePeriod: Long = 33,

    @Basic
    @Column(name = "delay", nullable = false)
    val delay: Long = 0,

    @Basic
    @Column(name = "algorithm_number", nullable = false)
    val methodNumber: Byte = 0,

    @Basic
    @Column(name = "distance_between_cameras", nullable = false)
    val distanceBetweenCameras: Double = 0.0,

    @Basic
    @Column(name = "ratio", nullable = true)
    val ratio: Double? = null,

    @Basic
    @Column(name = "quality_of_video", nullable = false)
    val qualityOfVideo: String = "HIGHEST",

    @Basic
    @Column(name = "measurement_number", nullable = false)
    val measurementNumber: Int = 10

)