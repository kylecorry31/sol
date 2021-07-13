package com.kylecorry.trailsensecore.domain.units

data class Weight(val weight: Float, val units: WeightUnits){
    fun convertTo(newUnits: WeightUnits): Weight {
        if (units == newUnits){
            return this
        }
        val grams = weight * units.grams
        return Weight(grams / newUnits.grams, newUnits)
    }
}
