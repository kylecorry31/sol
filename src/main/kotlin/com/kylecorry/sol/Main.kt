package com.kylecorry.sol

import com.google.gson.Gson
import com.kylecorry.sol.science.meteorology.Meteorology
import com.kylecorry.sol.science.meteorology.WeatherCondition
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.Reading
import java.io.File
import java.time.Duration
import java.time.Instant

fun main() {

    val examples = File("examples").listFiles()?.mapNotNull {
        try {
            val json = it.readText()
            val gson = Gson()
            gson.fromJson(json, Example::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } ?: listOf()

    examples.forEach {
        val zForecast = zambretti(it.input)
        val tForecast = trailSense(it.input)
        val aForecast = "${it.forecast.firstOrNull()?.shortForecast}. ${it.forecast.firstOrNull()?.longForecast}"
        val aForecast2 = "${it.forecast.getOrNull(1)?.shortForecast}. ${it.forecast.getOrNull(1)?.longForecast}"
        println("${it.latitude}, ${it.longitude}\nZambretti: $zForecast\nTrail Sense: $tForecast\nActual (today): $aForecast\nActual (tonight): $aForecast2\n\n")
    }


//    runBlocking {
//
//        val locations = listOf(
//            41.738 to -99.665
////            41.714 to -74.006,
////            28.538 to -81.379,
////            34.052 to -118.244,
////            61.218 to -149.900,
////            21.307 to -157.858,
//        )
//
//        locations.forEach { location ->
//
//            while (true) {
//                println("Loading example for ${location.first}, ${location.second}")
//                try {
//                    val example = Example(
//                        location.first,
//                        location.second,
//                        ZonedDateTime.now(),
//                        getInput(location.first, location.second),
//                        getForecast(location.first, location.second)
//                    )
//
//                    // Save the example to a file in JSON (using Gson)
//                    val gson = Gson()
//                    val json = gson.toJson(example)
//
//                    val folder = File("examples")
//                    folder.mkdirs()
//
//                    val file = File(folder, "${UUID.randomUUID()}.json")
//                    file.writeText(json)
//                    break
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                delay(1000)
//            }
//        }
//    }
}

data class Example(
    val latitude: Double,
    val longitude: Double,
    val time: Long?,
    val input: WeatherInput,
    val forecast: List<Forecast>
)

data class WeatherInput(
    val pressure: Float,
    val tendency: Float,
    val windDirection: Float? = null
)

private fun trailSense(input: WeatherInput): String {
    val reading1 = Reading(Pressure.hpa(input.pressure), Instant.now())
    val reading2 = Reading(Pressure.hpa(input.pressure - input.tendency), Instant.now().minus(Duration.ofHours(3)))

    val forecast = Meteorology.forecast(
        listOf(reading1, reading2).sortedBy { it.time },
        emptyList(),
        null
    )

    return forecast.first().conditions.toString()
}

private fun zambretti(input: WeatherInput): String {
    val pressure = input.pressure
    val tendency = input.tendency
    val windDirection = input.windDirection ?: 0f
    val season = "spring"

    val changeTendency = 1.6f

    val conditionLookup: Map<Int, List<WeatherCondition>> = mapOf(
        1 to listOf(WeatherCondition.Clear),
        2 to listOf(WeatherCondition.Clear),
        3 to listOf(WeatherCondition.Clear), // Wind later
        4 to listOf(WeatherCondition.Clear), // Precipitation later
        5 to listOf(WeatherCondition.Precipitation, WeatherCondition.Wind), // More precipitation later
        6 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation),
        7 to listOf(WeatherCondition.Precipitation), // More precipitation later
        8 to listOf(WeatherCondition.Precipitation), // More precipitation later
        9 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation),
        10 to listOf(WeatherCondition.Clear),
        11 to listOf(WeatherCondition.Clear),
        12 to listOf(WeatherCondition.Precipitation),
        13 to listOf(WeatherCondition.Precipitation),
        14 to listOf(WeatherCondition.Precipitation),
        15 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation),
        16 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation, WeatherCondition.Wind),
        17 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation),
        18 to listOf(WeatherCondition.Overcast, WeatherCondition.Precipitation, WeatherCondition.Wind),
        19 to listOf(WeatherCondition.Storm, WeatherCondition.Precipitation),
        20 to listOf(WeatherCondition.Clear),
        21 to listOf(WeatherCondition.Clear),
        22 to listOf(WeatherCondition.Clear),
        23 to listOf(WeatherCondition.Clear),
        24 to listOf(WeatherCondition.Clear), // Precipitation later
        25 to listOf(WeatherCondition.Precipitation), // Clear later
        26 to listOf(WeatherCondition.Overcast, WeatherCondition.Wind), // Clear later
        27 to listOf(WeatherCondition.Overcast, WeatherCondition.Wind), // Clear later
        28 to listOf(WeatherCondition.Overcast, WeatherCondition.Wind), // Clear later
        29 to listOf(WeatherCondition.Overcast, WeatherCondition.Wind),
        30 to listOf(WeatherCondition.Overcast, WeatherCondition.Wind),
        31 to listOf(WeatherCondition.Storm), // Clear later
        32 to listOf(WeatherCondition.Storm, WeatherCondition.Precipitation)
    )

    val lookup = mapOf(
        1 to "Settled Fine",
        2 to "Fine Weather",
        3 to "Fine, Becoming Less Settled",
        4 to "Fairly Fine, Showery Later",
        5 to "Showery, Becoming More Unsettled",
        6 to "Unsettled, Rain Later",
        7 to "Rain at Times, Worse Later",
        8 to "Rain at Times, Becoming Very Unsettled",
        9 to "Very Unsettled, Rain",
        10 to "Settled Fine",
        11 to "Fine Weather",
        12 to "Fine, Possibly Showers",
        13 to "Fairly Fine, Showers Likely",
        14 to "Showery, Bright Intervals",
        15 to "Changeable, Some Rain",
        16 to "Unsettled, Rain at Times",
        17 to "Rain at Frequent Intervals",
        18 to "Very Unsettled, Rain",
        19 to "Stormy, Much Rain",
        20 to "Settled Fine",
        21 to "Fine Weather",
        22 to "Becoming Fine",
        23 to "Fairly Fine, Improving",
        24 to "Fairly Fine, Possibly Showers Early",
        25 to "Showery Early, Improving",
        26 to "Changeable, Mending",
        27 to "Rather Unsettled, Clearing Later",
        28 to "Unsettled, Probably Improving",
        29 to "Unsettled, Short Fine Intervals",
        30 to "Very Unsettled, Finer at Times",
        31 to "Stormy, Possibly Improving",
        32 to "Stormy, Much Rain"
    )

    val z = if (tendency < changeTendency) {
        144 - 0.13 * pressure
    } else if (tendency > 0) {
        185 - 0.16 * pressure
    } else {
        127 - 0.12 * pressure
    }

    val windAdjustment = when {
        windDirection > 225 && windDirection <= 315 -> 1
        windDirection > 135 && windDirection <= 225 -> 2
        windDirection > 45 && windDirection <= 135 -> 1
        else -> 0
    }

    val seasonAdjustment = when {
        season == "winter" && tendency < 0 -> -1
        season == "summer" && tendency > 0 -> 1
        else -> 0
    }

    var zAdjusted = z + windAdjustment + seasonAdjustment

    if (zAdjusted < 1) {
        zAdjusted = 1.0
    } else if (zAdjusted > 32) {
        zAdjusted = 32.0
    }

//    println(zAdjusted)

    return "${conditionLookup[zAdjusted.toInt()]} ${lookup[zAdjusted.toInt()]}"
}

