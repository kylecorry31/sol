package com.kylecorry.sol.science.meteorology

import com.kylecorry.sol.math.sumOfFloat
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Temperature
import java.time.Month

internal object KoppenGeigerClimateClassifier {

    private val months1 = listOf(Month.APRIL, Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER)
    private val months2 =
        listOf(Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH)

    /**
     * Calculates the Koppen-Geiger climate classification
     * @param temperatures The average monthly temperatures
     * @param precipitation The average monthly precipitation
     * @return The Koppen-Geiger climate classification
     */
    fun classify(
        temperatures: Map<Month, Temperature>,
        precipitation: Map<Month, Distance>
    ): KoppenGeigerClimateClassification {
        // https://www.nature.com/articles/s41597-023-02549-6/tables/1
        // https://en.wikipedia.org/wiki/K%C3%B6ppen_climate_classification
        // https://open.oregonstate.education/permaculturedesign/back-matter/koppen-geiger-classification-descriptions
        requireValidTemperatures(temperatures)
        requireValidPrecipitations(precipitation)

        val factors = getClimateFactors(getTemperatureList(temperatures), getPrecipitationList(precipitation))
        val group = getClimateGroup(factors)
        val classification = when (group) {
            KoppenGeigerClimateGroup.Tropical -> getTropicalClassification(factors)
            KoppenGeigerClimateGroup.Dry -> getDryClassification(factors)
            KoppenGeigerClimateGroup.Temperate -> getTemperateClassification(factors)
            KoppenGeigerClimateGroup.Continental -> getContinentalClassification(factors)
            KoppenGeigerClimateGroup.Polar -> getPolarClassification(factors)
        }

        check(classification.climateGroup == group) { "Classification group does not match" }
        return classification
    }

    private fun getPolarClassification(
        factors: ClimateFactors
    ): KoppenGeigerClimateClassification {
        val seasonalPrecipitationPattern = when {
            factors.temperatureMax > 0 -> KoppenGeigerSeasonalPrecipitationPattern.Tundra
            else -> KoppenGeigerSeasonalPrecipitationPattern.IceCap
        }

        return KoppenGeigerClimateClassification(
            KoppenGeigerClimateGroup.Polar,
            seasonalPrecipitationPattern,
            null
        )
    }

    private fun getContinentalClassification(
        factors: ClimateFactors
    ): KoppenGeigerClimateClassification {
        val seasonalPrecipitationPattern = when {
            factors.precipitationSummerMin < 40 && factors.precipitationSummerMin < factors.precipitationWinterMax / 3 -> KoppenGeigerSeasonalPrecipitationPattern.DrySummer
            factors.precipitationWinterMin < factors.precipitationSummerMax / 10 -> KoppenGeigerSeasonalPrecipitationPattern.DryWinter
            else -> KoppenGeigerSeasonalPrecipitationPattern.NoDrySeason
        }
        val temperaturePattern = when {
            factors.temperatureMax >= 22f -> KoppenGeigerTemperaturePattern.HotSummer
            factors.temperatureCountOver10 >= 4 -> KoppenGeigerTemperaturePattern.WarmSummer
            factors.temperatureMin < -38 -> KoppenGeigerTemperaturePattern.VeryColdWinter
            else -> KoppenGeigerTemperaturePattern.ColdSummer
        }
        return KoppenGeigerClimateClassification(
            KoppenGeigerClimateGroup.Continental,
            seasonalPrecipitationPattern,
            temperaturePattern
        )
    }

    private fun getTemperateClassification(
        factors: ClimateFactors
    ): KoppenGeigerClimateClassification {
        var hasDrySummer =
            factors.precipitationSummerMin < 40 && factors.precipitationSummerMin < factors.precipitationWinterMax / 3
        var hasDryWinter = factors.precipitationWinterMin < factors.precipitationSummerMax / 10

        if (hasDryWinter && hasDrySummer) {
            hasDrySummer = factors.precipitationSummerTotal <= factors.precipitationWinterTotal
            hasDryWinter = !hasDrySummer
        }

        val seasonalPrecipitationPattern = when {
            hasDrySummer -> KoppenGeigerSeasonalPrecipitationPattern.DrySummer
            hasDryWinter -> KoppenGeigerSeasonalPrecipitationPattern.DryWinter
            else -> KoppenGeigerSeasonalPrecipitationPattern.NoDrySeason
        }
        val temperaturePattern = when {
            factors.temperatureMax >= 22f -> KoppenGeigerTemperaturePattern.HotSummer
            factors.temperatureCountOver10 >= 4 -> KoppenGeigerTemperaturePattern.WarmSummer
            factors.temperatureCountOver10 >= 1 -> KoppenGeigerTemperaturePattern.ColdSummer
            else -> null
        }
        return KoppenGeigerClimateClassification(
            KoppenGeigerClimateGroup.Temperate,
            seasonalPrecipitationPattern,
            temperaturePattern
        )
    }

    private fun getTropicalClassification(
        factors: ClimateFactors,
    ): KoppenGeigerClimateClassification {
        val seasonalPrecipitationPattern = when {
            factors.precipitationMin >= 60f -> KoppenGeigerSeasonalPrecipitationPattern.Rainforest
            factors.precipitationMin >= 100 - factors.precipitationAnnualMean / 25 -> KoppenGeigerSeasonalPrecipitationPattern.Monsoon
            // TODO: Replace Savanna with Wet Summer and Dry Summer
            else -> KoppenGeigerSeasonalPrecipitationPattern.Savanna
        }
        return KoppenGeigerClimateClassification(
            KoppenGeigerClimateGroup.Tropical,
            seasonalPrecipitationPattern,
            null
        )
    }

    private fun getDryClassification(
        factors: ClimateFactors,
    ): KoppenGeigerClimateClassification {
        val seasonalPrecipitationPattern = when {
            factors.precipitationAnnualMean < 5 * factors.precipitationThreshold -> KoppenGeigerSeasonalPrecipitationPattern.Desert
            else -> KoppenGeigerSeasonalPrecipitationPattern.Steppe
        }
        val temperaturePattern = if (factors.temperatureAnnualMean >= 18f) {
            KoppenGeigerTemperaturePattern.Hot
        } else {
            KoppenGeigerTemperaturePattern.Cold
        }
        return KoppenGeigerClimateClassification(
            KoppenGeigerClimateGroup.Dry,
            seasonalPrecipitationPattern,
            temperaturePattern
        )
    }

    private fun requireValidTemperatures(temperatures: Map<Month, Temperature>) {
        require(Month.entries.all { temperatures.contains(it) }) { "A temperature reading is required for each month" }
        require(temperatures.values.none { !it.value.isFinite() }) { "One or more temperature readings are invalid" }
    }

    private fun requireValidPrecipitations(precipitation: Map<Month, Distance>) {
        require(Month.entries.all { precipitation.contains(it) }) { "A precipitation reading is required for each month" }
        require(precipitation.values.none { !it.value.isFinite() || it.value < 0 }) { "One or more precipitation readings are invalid" }
    }

    private fun getPrecipitationList(precipitation: Map<Month, Distance>): List<Float> {
        return precipitation.entries.sortedBy { it.key.value }
            .map { it.value.convertTo(DistanceUnits.Millimeters).value }
    }

    private fun getTemperatureList(temperatures: Map<Month, Temperature>): List<Float> {
        return temperatures.entries.sortedBy { it.key.value }.map { it.value.celsius().value }
    }

    private fun isSouthernHemisphere(temps: List<Float>): Boolean {
        return months1.map { temps[it.value - 1] }.average() < months2.map { temps[it.value - 1] }.average()
    }

    private fun getWinterMonths(isSouthernHemisphere: Boolean): List<Month> {
        return if (isSouthernHemisphere) {
            listOf(Month.APRIL, Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER)
        } else {
            listOf(Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH)
        }
    }

    private fun getSummerMonths(isSouthernHemisphere: Boolean): List<Month> {
        return if (isSouthernHemisphere) {
            listOf(Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH)
        } else {
            listOf(Month.APRIL, Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER)
        }
    }

    private fun getClimateFactors(
        temperatures: List<Float>,
        precipitation: List<Float>,
    ): ClimateFactors {
        val isSouthernHemisphere = isSouthernHemisphere(temperatures)
        val winterMonths = getWinterMonths(isSouthernHemisphere)
        val summerMonths = getSummerMonths(isSouthernHemisphere)
        val mat = temperatures.average().toFloat()
        val tCold = temperatures.min()
        val tHot = temperatures.max()
        val tMon10 = temperatures.count { it > 10f }
        val map = precipitation.sum()
        val pDry = precipitation.min()
        val pSDry = summerMonths.minOf { precipitation[it.value - 1] }
        val pSWet = summerMonths.maxOf { precipitation[it.value - 1] }
        val pSTotal = summerMonths.sumOfFloat { precipitation[it.value - 1] }
        val pWDry = winterMonths.minOf { precipitation[it.value - 1] }
        val pWWet = winterMonths.maxOf { precipitation[it.value - 1] }
        val pWTotal = winterMonths.sumOfFloat { precipitation[it.value - 1] }
        val pThreshold = if (pWTotal / map > 0.7f) {
            2 * mat
        } else if (pSTotal / map > 0.7f) {
            2 * mat + 28f
        } else {
            2 * mat + 14f
        }
        return ClimateFactors(
            mat,
            tCold,
            tHot,
            tMon10,
            map,
            pDry,
            pSDry,
            pSWet,
            pSTotal,
            pWDry,
            pWWet,
            pWTotal,
            pThreshold
        )
    }

    private fun getClimateGroup(factors: ClimateFactors): KoppenGeigerClimateGroup {
        return when {
            factors.precipitationAnnualMean < 10 * factors.precipitationThreshold -> KoppenGeigerClimateGroup.Dry
            factors.temperatureMin >= 18f -> KoppenGeigerClimateGroup.Tropical
            factors.temperatureMax > 10 && factors.temperatureMin > 0 -> KoppenGeigerClimateGroup.Temperate
            factors.temperatureMax > 10 && factors.temperatureMin <= 0 -> KoppenGeigerClimateGroup.Continental
            else -> KoppenGeigerClimateGroup.Polar
        }
    }

    private data class ClimateFactors(
        val temperatureAnnualMean: Float,
        val temperatureMin: Float,
        val temperatureMax: Float,
        val temperatureCountOver10: Int,
        val precipitationAnnualMean: Float,
        val precipitationMin: Float,
        val precipitationSummerMin: Float,
        val precipitationSummerMax: Float,
        val precipitationSummerTotal: Float,
        val precipitationWinterMin: Float,
        val precipitationWinterMax: Float,
        val precipitationWinterTotal: Float,
        val precipitationThreshold: Float
    )

}
