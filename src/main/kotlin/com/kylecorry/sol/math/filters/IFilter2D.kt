package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2

interface IFilter2D {
    fun filter(data: List<Vector2>): List<Vector2>
}