package com.kylecorry.sol.math.statistics

import com.kylecorry.sol.tests.performanceTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class TextureTest {

    @Test
    fun features() {
        val glcm4x4 = arrayOf(
            arrayOf(0.3333f, 0.0833f, 0.0833f, 0f),
            arrayOf(0.0833f, 0f, 0.0833f, 0.0833f),
            arrayOf(0.0833f, 0.0833f, 0f, 0f),
            arrayOf(0f, 0.0833f, 0f, 0f)
        )
        val features = Texture.features(glcm4x4)
        assertEquals(2.02f, features.entropy, 0.1f)
        assertEquals(0.41f, features.energy, 0.1f)
        assertEquals(1.66f, features.contrast, 0.1f)
        assertEquals(0.57f, features.homogeneity, 0.1f)
        assertEquals(0.99f, features.dissimilarity, 0.1f)
        assertEquals(0.17f, features.angularSecondMoment, 0.1f)
        assertEquals(0.833f, features.horizontalMean, 0.1f)
        assertEquals(0.833f, features.verticalMean, 0.1f)
        assertEquals(0.97f, features.horizontalVariance, 0.1f)
        assertEquals(0.97f, features.verticalVariance, 0.1f)
        assertEquals(0.14f, features.correlation, 0.1f)
        assertEquals(0.33f, features.max, 0.1f)
    }

//    @Test
//    fun measurePerformance() {
//        val glcm = Array(255) { Array(255) { Math.random().toFloat() } }
//        performanceTest(10000) {
//            Texture.features(glcm)
//        }
//    }
}