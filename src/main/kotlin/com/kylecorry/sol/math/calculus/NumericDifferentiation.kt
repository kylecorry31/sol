package com.kylecorry.sol.math.calculus

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.arithmetic.Arithmetic
import kotlin.math.max
import kotlin.math.min

internal object NumericDifferentiation {

    private val FORWARD_STENCILS = mapOf(
        1 to Stencil(
            intArrayOf(0, 1),
            floatArrayOf(-1f, 1f)
        ),
        2 to Stencil(
            intArrayOf(0, 1, 2),
            floatArrayOf(-3f / 2f, 2f, -1f / 2f)
        ),
        3 to Stencil(
            intArrayOf(0, 1, 2, 3),
            floatArrayOf(-11f / 6f, 3f, -3f / 2f, 1f / 3f)
        )
    )

    private val CENTRAL_STENCILS = mapOf(
        1 to Stencil(
            intArrayOf(-1, 1),
            floatArrayOf(-0.5f, 0.5f)
        ),
        2 to Stencil(
            intArrayOf(-2, -1, 1, 2),
            floatArrayOf(1f / 12f, -2f / 3f, 2f / 3f, -1f / 12f)
        ),
        3 to Stencil(
            intArrayOf(-3, -2, -1, 1, 2, 3),
            floatArrayOf(-1f / 60f, 3f / 20f, -3f / 4f, 3f / 4f, -3f / 20f, 1f / 60f)
        )
    )

    private val BACKWARD_STENCILS = mapOf(
        1 to Stencil(
            intArrayOf(-1, 0),
            floatArrayOf(-1f, 1f)
        ),
        2 to Stencil(
            intArrayOf(-2, -1, 0),
            floatArrayOf(1f / 2f, -2f, 3f / 2f)
        ),
        3 to Stencil(
            intArrayOf(-3, -2, -1, 0),
            floatArrayOf(-1f / 3f, 3f / 2f, -3f, 11f / 6f)
        )
    )

    private fun stencilDifference(
        values: List<Vector2>,
        i: Int,
        stencil: Stencil
    ): Vector2 {
        // TODO: dx multiplier
        var derivative = 0f
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        val x = values[i].x
        val range = stencil.indices.last() - stencil.indices.first()
        for (j in stencil.indices.indices) {
            val index = i + stencil.indices[j]
            if (index < 0 || index >= values.size) {
                continue
            }
            derivative += values[index].y * stencil.coefficients[j]
            minX = min(minX, values[index].x)
            maxX = max(maxX, values[index].x)
        }
        val dx = maxX - minX
        if (Arithmetic.isZero(dx)) {
            return Vector2(x, 0f)
        }
        // Coefficients are based on the multiple of dx (space between points, which is why it is count - 1)
        derivative *= range
        return Vector2(x, derivative / dx)
    }

    fun finiteDifference(values: List<Vector2>, order: Int = 1): List<Vector2> {
        if (values.size < 2) {
            return emptyList()
        }
        val actualOrder = order.coerceIn(1, 3)

        val derivative = mutableListOf<Vector2>()
        // First point needs forward difference
        derivative.add(stencilDifference(values, 0, FORWARD_STENCILS[actualOrder]!!))
        for (j in 1 until values.size - 1) {
            val neighbors = min(j, values.size - 1 - j)
            val centerOrder = min(actualOrder, neighbors)
            derivative.add(stencilDifference(values, j, CENTRAL_STENCILS[centerOrder]!!))
        }
        // Last point needs backward difference
        derivative.add(stencilDifference(values, values.lastIndex, BACKWARD_STENCILS[actualOrder]!!))
        return derivative
    }

    // Second derivative
    // Central: (f(t + dt) - 2f(t) + f(t - dt)) / dt^2

    class Stencil(
        val indices: IntArray,
        val coefficients: FloatArray
    )

}