package com.kylecorry.trailsensecore.science.astronomy.units

import com.kylecorry.andromeda.core.math.*
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.trailsensecore.math.MathUtils
import kotlin.math.acos
import kotlin.math.asin

internal class HorizonCoordinate(_azimuth: Double, _altitude: Double) {

    val azimuth = wrap(_azimuth, 0.0, 360.0)
    val altitude = wrap(_altitude, -90.0, 90.0)

    fun toEquatorial(siderealTime: SiderealTime, latitude: Double): EquatorialCoordinate {
        val sinH = sinDegrees(altitude)
        val sinLat = sinDegrees(latitude)
        val cosLat = cosDegrees(latitude)

        val t0 = sinH * sinLat + cosDegrees(altitude) * cosLat * cosDegrees(azimuth)
        val declination = asin(t0).toDegrees()

        val t1 = sinH - sinLat * sinDegrees(declination)
        val t2 = cosLat * cosDegrees(declination)

        var hourAngle = acos(t1 / t2).toDegrees()

        val sinA = sinDegrees(azimuth)

        if (sinA > 0) {
            hourAngle = 360 - hourAngle
        }

        return EquatorialCoordinate.fromHourAngle(declination, hourAngle / 15, siderealTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HorizonCoordinate

        if (azimuth != other.azimuth) return false
        if (altitude != other.altitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = azimuth.hashCode()
        result = 31 * result + altitude.hashCode()
        return result
    }

    fun withRefraction(): HorizonCoordinate {
        val refraction = wrap(getRefraction(), -90.0, 90.0)
        return HorizonCoordinate(azimuth, altitude + refraction)
    }


    private fun getRefraction(): Double {
        if (altitude > 85.0) {
            return 0.0
        }

        val tanElev = tanDegrees(altitude)

        if (altitude > 5.0) {
            return (58.1 / tanElev - 0.07 / MathUtils.cube(tanElev) + 0.000086 / power(
                tanElev,
                5
            )) / 3600.0
        }

        if (altitude > -0.575) {
            return MathUtils.polynomial(altitude, 1735.0, -518.2, 103.4, -12.79, 0.711) / 3600.0
        }

        return -20.774 / tanElev / 3600.0
    }

    companion object {

        fun fromEquatorial(
            equatorial: EquatorialCoordinate,
            ut: UniversalTime,
            coordinate: Coordinate
        ): HorizonCoordinate {
            return fromEquatorial(
                equatorial,
                ut.toSiderealTime().atLongitude(coordinate.longitude),
                coordinate.latitude
            )
        }

        fun fromEquatorial(
            equatorial: EquatorialCoordinate,
            siderealTime: SiderealTime,
            latitude: Double
        ): HorizonCoordinate {
            val sinD = sinDegrees(equatorial.declination)
            val sinLat = sinDegrees(latitude)
            val cosLat = cosDegrees(latitude)

            val hourAngle = equatorial.getHourAngle(siderealTime) * 15

            val t0 =
                sinD * sinLat + cosDegrees(equatorial.declination) * cosLat * cosDegrees(hourAngle)
            val h = asin(t0).toDegrees()

            val t1 = sinD - sinLat * sinDegrees(h)

            var a = acos(t1 / (cosLat * cosDegrees(h))).toDegrees()

            val sinH = sinDegrees(hourAngle)

            if (sinH > 0) {
                a = 360 - a
            }

            return HorizonCoordinate(a, h)
        }
    }

}