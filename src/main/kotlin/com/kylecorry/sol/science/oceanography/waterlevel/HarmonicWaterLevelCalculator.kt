package com.kylecorry.sol.science.oceanography.waterlevel

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.science.oceanography.ConstituentCorrection
import com.kylecorry.sol.science.oceanography.TidalHarmonic
import com.kylecorry.sol.science.oceanography.TideConstituentCorrections
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class HarmonicWaterLevelCalculator(private val harmonics: List<TidalHarmonic>) :
    IWaterLevelCalculator {

    override fun calculate(time: ZonedDateTime): Float {
        val start = getStartDate(time.year)
        val corrections = getCorrections(time.year)
        val t = Duration.between(start, time).seconds / 3600f
        val heights = harmonics.map {
            corrections.getF(it.constituent) * it.amplitude * SolMath.cosDegrees(
                it.constituent.speed * t + corrections.getUV(
                    it.constituent
                ) - it.phase
            )
        }
        return heights.sum()
    }

    companion object {

        private fun getStartDate(year: Int): ZonedDateTime {
            return ZonedDateTime.of(
                LocalDateTime.of(getClosestYearWithData(year), 1, 1, 0, 0),
                ZoneId.of("UTC")
            )
        }

        private fun getCorrections(year: Int): TideConstituentCorrections {
            return when (getClosestYearWithData(year)) {
                2024 -> corrections2024
                2025 -> corrections2025
                else -> corrections2025
            }
        }

        private fun getClosestYearWithData(year: Int): Int {
            val yearsWithData = listOf(2024, 2025)
            return yearsWithData.minByOrNull { abs(it - year) } ?: yearsWithData.first()
        }

        private val corrections2024 = TideConstituentCorrections(
            ConstituentCorrection(1.1837230626740127f, 65.7634306026157f),
            ConstituentCorrection(1.1837230626740127f, 241.93791654123925f),
            ConstituentCorrection(1.0f, 349.84105553831614f),
            ConstituentCorrection(1.1134500118048742f, 6.987321819855424f),
            ConstituentCorrection(0.9654291266366927f, 71.3253040141426f),
            ConstituentCorrection(0.9654291266366927f, 247.49978995299898f),
            ConstituentCorrection(1.0f, 0.0f),
            ConstituentCorrection(1.2912235076190786f, 194.01027966105903f),
            ConstituentCorrection(0.9654291266366927f, 243.67427589156432f),
            ConstituentCorrection(0.9827145633183463f, 123.74989497649949f),
            ConstituentCorrection(1.1539047999567282f, 181.72210479492787f),
            ConstituentCorrection(0.8785347692640552f, 176.17448593885638f),
            ConstituentCorrection(1.4298200424975473f, 303.6239568705205f),
            ConstituentCorrection(0.948143689955039f, 191.24968492938206f),
        )

        private val corrections2025 = TideConstituentCorrections(
            ConstituentCorrection(1.195936272630187f, 36.03341654129326f),
            ConstituentCorrection(1.195936272630187f, 313.99553812877275f),
            ConstituentCorrection(1.0f, 349.0941103290388f),
            ConstituentCorrection(1.1209608093715056f, 10.673557053567492f),
            ConstituentCorrection(0.963012609158733f, 46.602554438402876f),
            ConstituentCorrection(0.963012609158733f, 324.56467602588236f),
            ConstituentCorrection(1.0f, 0.0f),
            ConstituentCorrection(1.3099025346108744f, 201.34972457219556f),
            ConstituentCorrection(0.963012609158733f, 62.52679761359468f),
            ConstituentCorrection(0.9815063045793665f, 342.2823380129412f),
            ConstituentCorrection(1.1649424068155168f, 288.53021528816316f),
            ConstituentCorrection(0.8700443024496025f, 277.9621215874795f),
            ConstituentCorrection(1.4568589137374195f, 236.57359976903535f),
            ConstituentCorrection(0.9445189137380996f, 126.84701403905638f),
        )
    }

}