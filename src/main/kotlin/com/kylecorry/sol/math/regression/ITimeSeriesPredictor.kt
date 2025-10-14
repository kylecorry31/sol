package com.kylecorry.sol.math.regression

import com.kylecorry.sol.math.Vector2

interface ITimeSeriesPredictor {
    fun predictNext(samples: List<Vector2>, n: Int, step: Float? = null): List<Vector2>
}