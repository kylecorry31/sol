package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.power
import com.kylecorry.sol.math.SolMath.sinDegrees
import com.kylecorry.sol.math.SolMath.tanDegrees
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import com.kylecorry.sol.units.DistanceUnits
import kotlin.math.*

internal class HorizonCoordinate(_azimuth: Double, _altitude: Double) {

    val azimuth = wrap(_azimuth, 0.0, 360.0)
    val altitude = wrap(_altitude, -90.0, 90.0)
    val zenith = 90 - altitude

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
            return (58.1 / tanElev - 0.07 / SolMath.cube(tanElev) + 0.000086 / power(
                tanElev,
                5
            )) / 3600.0
        }

        if (altitude > -0.575) {
            return SolMath.polynomial(altitude, 1735.0, -518.2, 103.4, -12.79, 0.711) / 3600.0
        }

        return -20.774 / tanElev / 3600.0
    }

    fun angularDistanceTo(coordinate: HorizonCoordinate): Double {
        return acos(
            cosDegrees(zenith) * cosDegrees(coordinate.zenith) +
                    sinDegrees(zenith) * sinDegrees(coordinate.zenith) *
                    cosDegrees(azimuth - coordinate.azimuth)
        ).toDegrees()
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

        fun fromEquatorial(
            equatorial: EquatorialCoordinate,
            ut: UniversalTime,
            coordinate: Coordinate,
            distanceToBody: Distance
        ): HorizonCoordinate {
            return fromEquatorial(
                equatorial,
                ut.toSiderealTime().atLongitude(coordinate.longitude),
                coordinate.latitude,
                distanceToBody
            )
        }

        fun fromEquatorial(
            equatorial: EquatorialCoordinate,
            siderealTime: SiderealTime,
            latitude: Double,
            distanceToBody: Distance
        ): HorizonCoordinate {
            val sinPi = 6378.14 / distanceToBody.convertTo(DistanceUnits.Kilometers).value
            val hourAngle = equatorial.getHourAngle(siderealTime) * 15

            val u = atan(0.99664719 * tanDegrees(latitude))
            val x = cos(u) // factor in elevation
            val y = 0.99664719 * sin(u) // factor in elevation

            val deltaAscension = atan2(
                -x * sinPi * sinDegrees(hourAngle),
                cosDegrees(equatorial.declination) - x * sinPi * cosDegrees(hourAngle)
            ).toDegrees()

            val trueDeclination = atan2(
                (sinDegrees(equatorial.declination) - y * sinPi) * cosDegrees(deltaAscension),
                cosDegrees(equatorial.declination) - y * sinPi * cosDegrees(hourAngle)
            ).toDegrees()

            return fromEquatorial(
                EquatorialCoordinate(trueDeclination, equatorial.rightAscension + deltaAscension),
                siderealTime,
                latitude
            )
        }
    }

}