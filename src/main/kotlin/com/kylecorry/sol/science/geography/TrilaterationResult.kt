package com.kylecorry.sol.science.geography

import com.kylecorry.sol.units.Coordinate

data class TrilaterationResult(val locations: List<Coordinate>, val biasDegrees: Float? = null)