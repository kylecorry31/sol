package com.kylecorry.sol.science.geophysics

import com.kylecorry.sol.units.Coordinate

fun interface VerticalDeflectionModel {
    fun getVerticalDeflection(location: Coordinate): VerticalDeflection?
}
