package com.kylecorry.trailsensecore.domain.weather

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class WeatherServiceTest {

    private val weatherService = WeatherService()

    @Test
    fun classifyPressure() {
        val cases = listOf(
            Pair(1030f, PressureClassification.High),
            Pair(1022.689f, PressureClassification.High),
            Pair(1000f, PressureClassification.Low),
            Pair(1009.144f, PressureClassification.Low),
            Pair(1009.145f, PressureClassification.Normal),
            Pair(1022.688f, PressureClassification.Normal),
            Pair(1013f, PressureClassification.Normal),
        )

        for (case in cases) {
            assertEquals(
                case.second,
                weatherService.classifyPressure(PressureReading(Instant.now(), case.first))
            )
        }
    }
}