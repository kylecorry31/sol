package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.units.Coordinate

interface CoordinateFormat {
    fun toString(coordinate: Coordinate): String
    fun parse(text: String): Coordinate?
}