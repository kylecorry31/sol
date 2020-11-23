package com.kylecorry.trailsensecore.domain.geo

import android.location.Location
import android.os.Parcelable
import com.kylecorry.trailsensecore.domain.math.cosDegrees
import com.kylecorry.trailsensecore.domain.math.roundPlaces
import com.kylecorry.trailsensecore.domain.math.sinDegrees
import com.kylecorry.trailsensecore.domain.math.toDegrees
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.math.*


@Parcelize
data class Coordinate(val latitude: Double, val longitude: Double) : Parcelable {

    private val latitudeDMS: String
        get() {
            val direction = if (latitude < 0) "S" else "N"
            return "${dmsString(latitude)} $direction"
        }

    private val longitudeDMS: String
        get() {
            val direction = if (longitude < 0) "W" else "E"
            return "${dmsString(longitude)} $direction"
        }

    override fun toString(): String {
        return "$latitudeDMS, $longitudeDMS"
    }


    fun distanceTo(other: Coordinate): Float {
        val results = FloatArray(3)
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, results)
        return results[0]
    }

    fun plus(meters: Double, bearing: Bearing): Coordinate {
        // Adapted from https://www.movable-type.co.uk/scripts/latlong.html
        val radius = 6371e3
        val newLat = asin(
            sinDegrees(latitude) * cos(meters / radius) +
                    cosDegrees(latitude) * sin(meters / radius) * cosDegrees(bearing.value.toDouble())
        ).toDegrees()

        val newLng = longitude + atan2(
            sinDegrees(bearing.value.toDouble()) * sin(meters / radius) * cosDegrees(latitude),
            cos(meters / radius) - sinDegrees(latitude) * sinDegrees(newLat)
        ).toDegrees()

        val normalLng = (newLng + 540) % 360 - 180

        return Coordinate(newLat, normalLng)
    }

    fun toUTM(): String {
        // From https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
        val zone = floor(longitude / 6 + 31).toInt()
        val letter =
            if (latitude < -72) 'C' else if (latitude < -64) 'D' else if (latitude < -56) 'E' else if (latitude < -48) 'F' else if (latitude < -40) 'G' else if (latitude < -32) 'H' else if (latitude < -24) 'J' else if (latitude < -16) 'K' else if (latitude < -8) 'L' else if (latitude < 0) 'M' else if (latitude < 8) 'N' else if (latitude < 16) 'P' else if (latitude < 24) 'Q' else if (latitude < 32) 'R' else if (latitude < 40) 'S' else if (latitude < 48) 'T' else if (latitude < 56) 'U' else if (latitude < 64) 'V' else if (latitude < 72) 'W' else 'X'
        var easting = 0.5 * ln(
            (1 + cos(latitude * Math.PI / 180) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - cos(
                latitude * Math.PI / 180
            ) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))
        ) * 0.9996 * 6399593.62 / (1 + 0.0820944379.pow(2.0) * cos(latitude * Math.PI / 180)
            .pow(2.0)).pow(0.5) * (1 + 0.0820944379.pow(2.0) / 2 * (0.5 * Math.log(
            (1 + cos(latitude * Math.PI / 180) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180)) / (1 - cos(
                latitude * Math.PI / 180
            ) * sin(longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180))
        )).pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0) / 3) + 500000
        easting = (easting * 100).roundToInt() * 0.01
        var northing = (atan(
            tan(latitude * Math.PI / 180) / cos(
                longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180
            )
        ) - latitude * Math.PI / 180) * 0.9996 * 6399593.625 / sqrt(
            1 + 0.006739496742 * cos(latitude * Math.PI / 180).pow(2.0)
        ) * (1 + 0.006739496742 / 2 * (0.5 * ln(
            (1 + cos(latitude * Math.PI / 180) * sin(
                longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180
            )) / (1 - cos(latitude * Math.PI / 180) * sin(
                longitude * Math.PI / 180 - (6 * zone - 183) * Math.PI / 180
            ))
        )).pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0)) + 0.9996 * 6399593.625 * (latitude * Math.PI / 180 - 0.005054622556 * (latitude * Math.PI / 180 + sin(
            2 * latitude * Math.PI / 180
        ) / 2) + 4.258201531e-05 * (3 * (latitude * Math.PI / 180 + sin(2 * latitude * Math.PI / 180) / 2) + sin(
            2 * latitude * Math.PI / 180
        ) * cos(latitude * Math.PI / 180).pow(2.0)) / 4 - 1.674057895e-07 * (5 * (3 * (latitude * Math.PI / 180 + sin(
            2 * latitude * Math.PI / 180
        ) / 2) + sin(
            2 * latitude * Math.PI / 180
        ) * cos(latitude * Math.PI / 180).pow(2.0)) / 4 + sin(2 * latitude * Math.PI / 180) * cos(
            latitude * Math.PI / 180
        ).pow(2.0) * cos(latitude * Math.PI / 180).pow(2.0)) / 3)
        if (letter < 'N') northing += 10000000
        northing = (northing * 100).roundToInt() * 0.01

        return "${zone.toString().padStart(2, '0')} $letter ${easting.toInt()} ${northing.toInt()}"
    }

    /**
     * Get the bearing to the other coordinate (using True North)
     */
    fun bearingTo(other: Coordinate): Bearing {
        val results = FloatArray(3)
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, results)
        return Bearing(results[1])
    }

    private fun dmsString(degrees: Double): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60)
        val seconds = abs(((minutes % 1) * 60).roundPlaces(1))
        return "$deg°${minutes.toInt()}'$seconds\""
    }

    companion object {

        val zero = Coordinate(0.0, 0.0)

        fun parseLatitude(latitude: String): Double? {

            val dms =
                parseDMS(
                    latitude,
                    true
                )
            if (dms != null) {
                return dms
            }

            val ddm =
                parseDDM(
                    latitude,
                    true
                )
            if (ddm != null) {
                return ddm
            }

            return parseDecimal(
                latitude,
                true
            )
        }

        fun parseLongitude(longitude: String): Double? {
            val dms =
                parseDMS(
                    longitude,
                    false
                )
            if (dms != null) {
                return dms
            }

            val ddm =
                parseDDM(
                    longitude,
                    false
                )
            if (ddm != null) {
                return ddm
            }

            return parseDecimal(
                longitude,
                false
            )
        }

        private fun parseDecimal(latOrLng: String, isLatitude: Boolean): Double? {
            try {
                val number = latOrLng.toDoubleOrNull() ?: return null

                return if (isLatitude && isValidLatitude(
                        number
                    )
                ) {
                    number
                } else if (!isLatitude && isValidLongitude(
                        number
                    )
                ) {
                    number
                } else {
                    null
                }
            } catch (e: Exception) {
                return null
            }
        }

        private fun parseDMS(latOrLng: String, isLatitude: Boolean): Double? {
            try {
                val dmsRegex = if (isLatitude) {
                    Regex("(\\d+)°\\s*(\\d+)'\\s*([\\d.]+)\"\\s*([nNsS])")
                } else {
                    Regex("(\\d+)°\\s*(\\d+)'\\s*([\\d.]+)\"\\s*([wWeE])")
                }
                val matches = dmsRegex.find(latOrLng) ?: return null

                var decimal = 0.0
                decimal += matches.groupValues[1].toDouble()
                decimal += matches.groupValues[2].toDouble() / 60
                decimal += matches.groupValues[3].toDouble() / (60 * 60)
                decimal *= if (isLatitude) {
                    if (matches.groupValues[4].toLowerCase(Locale.getDefault()) == "n") 1 else -1
                } else {
                    if (matches.groupValues[4].toLowerCase(Locale.getDefault()) == "e") 1 else -1
                }

                return if (isLatitude && isValidLatitude(
                        decimal
                    )
                ) {
                    decimal
                } else if (!isLatitude && isValidLongitude(
                        decimal
                    )
                ) {
                    decimal
                } else {
                    null
                }
            } catch (e: Exception) {
                return null
            }
        }

        private fun parseDDM(latOrLng: String, isLatitude: Boolean): Double? {
            try {
                val dmsRegex = if (isLatitude) {
                    Regex("(\\d+)°\\s*([\\d.]+)'\\s*([nNsS])")
                } else {
                    Regex("(\\d+)°\\s*([\\d.]+)'\\s*([wWeE])")
                }
                val matches = dmsRegex.find(latOrLng) ?: return null

                var decimal = 0.0
                decimal += matches.groupValues[1].toDouble()
                decimal += matches.groupValues[2].toDouble() / 60
                decimal *= if (isLatitude) {
                    if (matches.groupValues[3].toLowerCase(Locale.getDefault()) == "n") 1 else -1
                } else {
                    if (matches.groupValues[3].toLowerCase(Locale.getDefault()) == "e") 1 else -1
                }

                return if (isLatitude && isValidLatitude(
                        decimal
                    )
                ) {
                    decimal
                } else if (!isLatitude && isValidLongitude(
                        decimal
                    )
                ) {
                    decimal
                } else {
                    null
                }
            } catch (e: Exception) {
                return null
            }
        }

        private fun isValidLongitude(longitude: Double): Boolean {
            return longitude.absoluteValue <= 180
        }

        private fun isValidLatitude(latitude: Double): Boolean {
            return latitude.absoluteValue <= 90
        }
    }
}