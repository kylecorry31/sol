package com.kylecorry.trailsensecore.meteorology

interface ISeaLevelPressureConverter {

    fun convert(readings: List<PressureAltitudeReading>, factorInTemperature: Boolean = false): List<PressureReading>

}