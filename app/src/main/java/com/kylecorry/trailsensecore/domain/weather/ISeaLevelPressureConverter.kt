package com.kylecorry.trailsensecore.domain.weather

interface ISeaLevelPressureConverter {

    fun convert(readings: List<PressureAltitudeReading>, factorInTemperature: Boolean = false): List<PressureReading>

}