package com.kylecorry.sol.math

interface IInclinationService {

    /**
     * Determines the grade (decimal, not percent)
     * @param inclination The inclination angle (degrees)
     */
    fun grade(inclination: Float): Float

    /**
     * Estimates the height of an object
     * @param distance The distance to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object
     */
    fun height(distance: Float, bottomInclination: Float, topInclination: Float): Float

    /**
     * Estimates the distance to an object
     * @param height The height to the object
     * @param bottomInclination The inclination angle to the bottom (degrees)
     * @param topInclination The inclination angle to the top (degrees)
     * @return The estimated height of the object
     */
    fun distance(height: Float, bottomInclination: Float, topInclination: Float): Float
}