package com.kylecorry.sol.math.geometry

import com.kylecorry.sol.math.Range
import org.junit.jupiter.api.Test

import kotlin.math.sqrt

class TrilaterationTest {

    @Test
    fun trilaterate() {

        /*
        (0, 0, (10, 20)),
    (1, 0, (5, 18))
         */
        val points = listOf(
            Pair(0.0, 0.0) to Range(10.0, 20.0),
            Pair(1.0, 0.0) to Range(5.0, 18.0)
        )

        val result = Trilateration.trilaterate(
            points
        ) { a, b ->
            val dx = a.first - b.first
            val dy = a.second - b.second
            sqrt(dx * dx + dy * dy)
        }

        println(result)

    }
}