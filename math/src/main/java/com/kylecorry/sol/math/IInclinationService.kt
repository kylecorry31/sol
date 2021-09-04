package com.kylecorry.sol.math

interface IInclinationService {

    /**
     * Estimates the height of an object
     * @param distance The distance to the object (m)
     * @param inclination The inclination angle (degrees)
     * @param phoneHeight The phone height (m)
     * @return The estimated height of the object (m)
     */
    fun estimateHeight(distance: Float, inclination: Float, phoneHeight: Float): Float

    /**
     * Estimates the height of an object
     * @param distance The distance to the object (m)
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object (m)
     */
    fun estimateHeightAngles(distance: Float, bottomInclination: Float, topInclination: Float): Float
}