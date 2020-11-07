package com.kylecorry.trailsensecore.domain.depth

import org.junit.Assert
import org.junit.Test

internal class DepthServiceTest {
    @Test
    fun canCalculateDepth(){
        val currentPressure = 2222.516f
        val service = DepthService()

        val depth = service.calculateDepth(currentPressure, 1013f)

        val expected = 12f

        Assert.assertEquals(expected, depth, 0.1f)
    }

    @Test
    fun returnsZeroWhenAboveWater(){
        val currentPressure = 1000f
        val service = DepthService()

        val depth = service.calculateDepth(currentPressure, 1013f)

        val expected = 0f

        Assert.assertEquals(expected, depth, 0.0001f)
    }
}