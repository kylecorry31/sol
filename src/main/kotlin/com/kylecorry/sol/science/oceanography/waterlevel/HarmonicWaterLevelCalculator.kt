package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.trigonometry.Trigonometry
import com.kylecorry.sol.science.oceanography.TidalHarmonic
import java.time.ZonedDateTime

class HarmonicWaterLevelCalculator(private val harmonics: List<TidalHarmonic>) :
    IWaterLevelCalculator {

    private class NodalCorrections(val day: Long, val f: DoubleArray, val u: DoubleArray)

    @Volatile
    private var nodalCorrections: NodalCorrections? = null

    override fun calculate(time: ZonedDateTime): Float {
        val nodal = getNodalCorrections(time)
        val astronomical = AstronomicalArgument.at(time.toEpochSecond() + time.nano / 1e9)
        var height = 0.0
        for (i in harmonics.indices) {
            val harmonic = harmonics[i]
            val argument = astronomical.of(harmonic.constituent) ?: continue
            height += nodal.f[i] * harmonic.amplitude *
                    Trigonometry.cosDegrees(argument + nodal.u[i] - harmonic.phase)
        }
        return height.toFloat()
    }

    // The nodal terms change slowly, so they are only recomputed when the day changes
    private fun getNodalCorrections(time: ZonedDateTime): NodalCorrections {
        val day = Math.floorDiv(time.toEpochSecond(), SECONDS_PER_DAY)
        nodalCorrections?.let { if (it.day == day) return it }

        val nodal = NodalCorrection.at(day * SECONDS_PER_DAY)
        val f = DoubleArray(harmonics.size)
        val u = DoubleArray(harmonics.size)
        for (i in harmonics.indices) {
            val (nodalF, nodalU) = nodal.of(harmonics[i].constituent)
            f[i] = nodalF
            u[i] = nodalU
        }
        return NodalCorrections(day, f, u).also { nodalCorrections = it }
    }

    private companion object {
        const val SECONDS_PER_DAY = 86400L
    }

}
