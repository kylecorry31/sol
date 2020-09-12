package com.kylecorry.trailsensecore.domain.inclinometer

interface IInclinationService {
    /**
     * Determine the avalanche risk of a slope
     * @param inclination The inclination angle (degrees)
     * @return The avalanche risk
     */
    fun getAvalancheRisk(inclination: Float): AvalancheRisk

    /**
     * Estimates the height of an object
     * @param distance The distance to the object (m)
     * @param inclination The inclination angle (degrees)
     * @param phoneHeight The phone height (m)
     * @return The estimated height of the object (m)
     */
    fun estimateHeight(distance: Float, inclination: Float, phoneHeight: Float): Float
}