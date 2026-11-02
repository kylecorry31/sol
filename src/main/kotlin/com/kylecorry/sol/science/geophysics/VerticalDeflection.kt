package com.kylecorry.sol.science.geophysics

/**
 * The deflection of the vertical: the angle between gravity (the plumb line) and the ellipsoid
 * normal, in arc seconds.
 * @param xi the north-south component (positive when gravity's zenith is north of the ellipsoid's)
 * @param eta the east-west component (positive when gravity's zenith is east of the ellipsoid's)
 */
data class VerticalDeflection(val xi: Double, val eta: Double)
