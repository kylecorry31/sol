package com.kylecorry.sol.math.filters

import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.geometry.Geometry
import com.kylecorry.sol.math.geometry.Line
import com.kylecorry.sol.tests.performanceTest
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class RDPFilterTest {

    @Test
    fun filter() {

        val points = listOf(
            Vector2(0f, 0f),
            Vector2(1f, 1f),
            Vector2(2f, 2f),
            Vector2(3f, 1f),
            Vector2(4f, 1f)
        )

        val expected1 = listOf(
            Vector2(0f, 0f),
            Vector2(2f, 2f),
            Vector2(4f, 1f)
        )

        val expected2 = listOf(
            Vector2(0f, 0f),
            Vector2(2f, 2f),
            Vector2(3f, 1f),
            Vector2(4f, 1f)
        )

        val rdp1 =
            RDPFilter<Vector2>(0.5f) { point, start, end ->
                Geometry.pointLineDistance(
                    point,
                    Line(start, end)
                )
            }

        val rdp2 =
            RDPFilter<Vector2>(0.01f) { point, start, end ->
                Geometry.pointLineDistance(
                    point,
                    Line(start, end)
                )
            }

        val filtered1 = rdp1.filter(points)

        assertEquals(expected1, filtered1)

        val filtered2 = rdp2.filter(points)

        assertEquals(expected2, filtered2)

        assertEquals(emptyList<Vector2>(), rdp1.filter(emptyList()))
    }

//    @Test
//    fun performance(){
//        val random = Random(1)
//        val points = (0..10000).map { Vector2(it.toFloat(), random.nextFloat() * 100f) }
//
//        val rdp = RDPFilter<Vector2>(10f) { point, start, end ->
//            Geometry.pointLineDistance(
//                point,
//                Line(start, end)
//            )
//        }
//        performanceTest(10000) {
//            rdp.filter(points)
//        }
//    }
}