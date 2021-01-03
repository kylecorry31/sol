package com.kylecorry.trailsensecore.domain.units

data class Distance(val distance: Float, val units: DistanceUnits) {

    private val unitService = UnitService()

    fun convertTo(newUnits: DistanceUnits): Distance {
        val newDistance = unitService.convert(distance, units, newUnits)
        return Distance(newDistance, newUnits)
    }

}