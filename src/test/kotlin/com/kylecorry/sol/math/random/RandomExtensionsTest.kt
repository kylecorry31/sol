package com.kylecorry.sol.math.random

import com.kylecorry.sol.math.statistics.Statistics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class RandomExtensionsTest {

    @Test
    fun nextGaussianHasBoundedAttempts() {
        val random = object : Random() {
            override fun nextBits(bitCount: Int): Int {
                return 0
            }
        }

        assertThrows(IllegalStateException::class.java) {
            random.nextGaussian()
        }
    }

    @Test
    fun nextGaussian() {
        val random = Random(12345)
        val samples = List(100) { random.nextGaussian().toFloat() }
        val mean = Statistics.mean(samples)
        val stdDev = Statistics.stdev(samples, mean = mean)

        assertEquals(0f, mean, 0.1f)
        assertEquals(1f, stdDev, 0.1f)
    }
}
