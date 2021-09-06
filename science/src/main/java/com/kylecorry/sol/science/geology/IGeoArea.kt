package com.kylecorry.sol.science.geology

import com.kylecorry.sol.units.Coordinate

interface IGeoArea {
    fun contains(location: Coordinate): Boolean
}