package com.kylecorry.trailsensecore.science.meteorology

interface ISeaLevelPressureConverter {

    fun convert(readings: List<PressureAltitudeReading>, factorInTemperature: Boolean = false): List<PressureReading>

}