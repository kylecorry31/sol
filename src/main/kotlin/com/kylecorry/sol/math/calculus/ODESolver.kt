package com.kylecorry.sol.math.calculus

import com.kylecorry.sol.math.Range
import com.kylecorry.sol.math.Vector
import com.kylecorry.sol.math.Vector2

interface ODESolver {
    fun solve(
        x: Range<Float>,
        stepSize: Float,
        initialY: Vector,
        derivative: (x: Float, y: Vector) -> Vector
    ): List<Pair<Float, Vector>>
}

fun ODESolver.solve(
    x: Range<Float>,
    stepSize: Float,
    initialY: Vector2,
    derivative: (x: Float, y: Vector2) -> Vector2
): List<Pair<Float, Vector2>> {
    val result = solve(
        x,
        stepSize,
        Vector.from(initialY.x, initialY.y)
    ) { x, y ->
        val vec = derivative(x, Vector2(y[0], y[1]))
        Vector.from(vec.x, vec.y)
    }
    return result.map { Pair(it.first, Vector2(it.second[0], it.second[1])) }
}

fun ODESolver.solve(
    x: Range<Float>,
    stepSize: Float,
    initialY: Float,
    derivative: (x: Float, y: Float) -> Float
): List<Pair<Float, Float>> {
    val result = solve(
        x,
        stepSize,
        Vector.from(initialY)
    ) { x, y ->
        Vector.from(derivative(x, y[0]))
    }
    return result.map { Pair(it.first, it.second[0]) }
}