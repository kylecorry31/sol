package com.kylecorry.trailsensecore.domain.units

data class Weight(val weight: Float, val units: WeightUnits){
    fun convertTo(newUnits: WeightUnits): Weight {
        if (units == newUnits){
            return this
        }
        val kg = weight * units.kilograms
        return Weight(kg / newUnits.kilograms, newUnits)
    }
}
