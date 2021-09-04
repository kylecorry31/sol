package com.kylecorry.sol.units

data class Volume(val volume: Float, val units: VolumeUnits) {
    fun convertTo(newUnits: VolumeUnits): Volume {
        if (units == newUnits) {
            return this
        }
        val l = volume * units.liters
        return Volume(l / newUnits.liters, newUnits)
    }
}