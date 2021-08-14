package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.domain.math.*
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.*

class SolarPanelService() {


    fun getOptimalPosition(start: ZonedDateTime, end: ZonedDateTime, location: Coordinate): SolarPanelPosition {
        // Optimize
        val startTilt = 0f
        val endTilt = 90f

        val startAzimuth = 180f //if (!location.isNorthernHemisphere) 90f else 270f
        val endAzimuth = 180f//360f//if (!location.isNorthernHemisphere) 270f else 450f

        var tilt: Float
        var azimuth = startAzimuth


        var maxTilt = startTilt
        var maxAzimuth = startAzimuth
        var maxRadiation = 0.0

        val increment = 1f

        var lastAzimuthRadiation = 0.0
        while (azimuth <= endAzimuth){

            var lastTiltRadiation = 0.0
            tilt = startTilt
            while (tilt <= endTilt){
                val radiation = getSolarRadiationOnPanel(start, end, location, SolarPanelPosition(tilt, Bearing(azimuth)))
                if (radiation > maxRadiation){
                    maxRadiation = radiation
                    maxTilt = tilt
                    maxAzimuth = azimuth
                }

//                if (radiation < lastTiltRadiation){
//                    break
//                }

                println("${tilt}, ${(radiation / 1000000000.0).roundPlaces(2)}")

                lastTiltRadiation = radiation

                tilt += increment
            }

            if (lastTiltRadiation < lastAzimuthRadiation){
                break
            }

            println("${azimuth}, ${(lastTiltRadiation / 1000000000.0).roundPlaces(2)}")

            lastAzimuthRadiation = lastTiltRadiation
            azimuth += increment
        }

        return SolarPanelPosition(90 - maxTilt, Bearing(maxAzimuth).inverse())
    }

    fun getSolarRadiationOnPanel(
        start: ZonedDateTime,
        end: ZonedDateTime,
        location: Coordinate,
        position: SolarPanelPosition
    ): Double {
        val increment = Duration.ofMinutes(10)

        var time = start
        var sum = 0.0

        while (time <= end){
            val radiation = getSolarRadiationOnPanel(time, location, position)
            if (radiation > 0f){
                sum += radiation
            }
            time = time.plus(increment)
        }

        return sum
    }

    fun getSolarRadiationOnPanel(
        time: ZonedDateTime,
        location: Coordinate,
        position: SolarPanelPosition
    ): Double {
        val dayOfYear = time.dayOfYear
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = wrap(Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        ), 0.0, 360.0)
        return (DAY_RADIANS * getSolarRadiation(
            dayOfYear,
            location.latitude,
            solarCoordinates.declination,
            hourAngle
        ) * getCosAngleOfIncidence(
            location.latitude,
            position.tilt.toDouble(),
            position.bearing.value.toDouble(),
            hourAngle,
            solarCoordinates.declination
        ))
    }

    fun getSolarRadiation(time: ZonedDateTime, location: Coordinate): Float {
        val dayOfYear = time.dayOfYear
        val ut = Astro.ut(time)
        val jd = Astro.julianDay(ut)
        val solarCoordinates = Astro.solarCoordinates(jd)
        val hourAngle = wrap(Astro.hourAngle(
            Astro.meanSiderealTime(jd),
            location.longitude,
            solarCoordinates.rightAscension
        ), 0.0, 360.0)
        return getSolarRadiation(
            dayOfYear,
            location.latitude,
            solarCoordinates.declination,
            hourAngle
        ).toFloat()
    }

    private fun getSolarRadiation(
        dayOfYear: Int,
        latitude: Double,
        declination: Double,
        hourAngle: Double
    ): Double {
        val Ic = 1367
        return (24 * 3600) / PI * Ic * (1 + 0.033 * cos(2 * PI * dayOfYear / 365.0)) * (cosDegrees(
            latitude
        ) * cosDegrees(declination) * sinDegrees(hourAngle) + 2 * PI * hourAngle / 360.0 * sinDegrees(
            latitude
        ) * sinDegrees(declination))
    }

    private fun getCosAngleOfIncidence(
        latitude: Double,
        tilt: Double,
        azimuth: Double,
        hourAngle: Double,
        declination: Double
    ): Double {
        val sinDec = sinDegrees(declination)
        val cosDec = cosDegrees(declination)
        val sinLat = sinDegrees(latitude)
        val cosLat = cosDegrees(latitude)
        val sinTilt = sinDegrees(tilt)
        val cosTilt = cosDegrees(tilt)
        val sinAzimuth = sinDegrees(azimuth)
        val cosAzimuth = cosDegrees(azimuth)
        val sinHour = sinDegrees(hourAngle)
        val cosHour = cosDegrees(hourAngle)



        return (sinDec * sinLat * cosTilt
                - sinDec * cosLat * sinTilt * cosAzimuth
                + cosDec * cosLat * cosTilt * cosHour
                + cosDec * sinLat * sinTilt * cosAzimuth * cosHour
                + cosDec * sinTilt * sinAzimuth * sinHour)
    }

    companion object {
        private const val DAY_RADIANS = 24.36 / (2 * PI)
    }

}