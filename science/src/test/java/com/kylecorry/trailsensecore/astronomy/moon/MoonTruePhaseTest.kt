package com.kylecorry.trailsensecore.astronomy.moon

import com.kylecorry.trailsensecore.astronomy.moon.MoonTruePhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class MoonTruePhaseTest {

    @ParameterizedTest
    @MethodSource("provideAngles")
    fun anglesAreCorrect(phase: MoonTruePhase, start: Float, end: Float){
        assertEquals(start, phase.startAngle, 0.001f)
        assertEquals(end, phase.endAngle, 0.001f)
    }

    companion object {
        @JvmStatic
        fun provideAngles(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(MoonTruePhase.New, 348.75f, 11.25f),
                Arguments.of(MoonTruePhase.WaningCrescent, 11.25f, 78.75f),
                Arguments.of(MoonTruePhase.ThirdQuarter, 78.75f, 101.25f),
                Arguments.of(MoonTruePhase.WaningGibbous, 101.25f, 168.75f),
                Arguments.of(MoonTruePhase.Full, 168.75f, 191.25f),
                Arguments.of(MoonTruePhase.WaxingGibbous, 191.25f, 258.75f),
                Arguments.of(MoonTruePhase.FirstQuarter, 258.75f, 281.25f),
                Arguments.of(MoonTruePhase.WaxingCrescent, 281.25f, 348.75f),
            )
        }
    }

}