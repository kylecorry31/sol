package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.math.SolMath.cosDegrees
import com.kylecorry.sol.math.SolMath.normalizeAngle
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
        return 1.02 / tanDegrees(altitude + 10.3 / (altitude + 5.11)) / 60.0
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
            val sinPi = 6378.14 / distanceToBody.convertTo(DistanceUnits.Kilometers).distance
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

            val hPrime = hourAngle - deltaAscension

            var altitude = wrap(asin(
                sinDegrees(latitude) * sinDegrees(trueDeclination) +
                        cosDegrees(latitude) * cosDegrees(trueDeclination) * cosDegrees(hPrime)
            ).toDegrees(), -90.0, 90.0)

            val astronomersAzimuth = atan2(
                sinDegrees(hPrime),
                cosDegrees(hPrime) * sinDegrees(latitude) - tanDegrees(trueDeclination) * cosDegrees(
                    latitude
                )
            ).toDegrees()

            val sinH = sinDegrees(hPrime)

            if (sinH > 0) {
                altitude = 360 - altitude
            }

            return fromEquatorial(
                EquatorialCoordinate(trueDeclination, equatorial.rightAscension + deltaAscension),
                siderealTime,
                latitude
            )

//            return HorizonCoordinate(astronomersAzimuth + 180, altitude)

        }
    }

}