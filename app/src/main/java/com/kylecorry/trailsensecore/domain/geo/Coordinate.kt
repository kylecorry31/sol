package com.kylecorry.trailsensecore.domain.geo

import android.location.Location
import android.os.Parcelable
import com.kylecorry.trailsensecore.domain.astronomy.Astro.power
import com.kylecorry.trailsensecore.domain.math.cosDegrees
import com.kylecorry.trailsensecore.domain.math.roundPlaces
import com.kylecorry.trailsensecore.domain.math.sinDegrees
import com.kylecorry.trailsensecore.domain.math.toDegrees
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.MGRSCoord
import gov.nasa.worldwind.geom.coords.UTMCoord
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.math.*


@Parcelize
data class Coordinate(val latitude: Double, val longitude: Double) : Parcelable {

    override fun toString(): String {
        return toDecimalDegrees()
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

    fun toDecimalDegrees(precision: Int = 7): String {
        return "${latitude.roundPlaces(precision)}°,  ${longitude.roundPlaces(precision)}°"
    }

    fun toDegreeDecimalMinutes(precision: Int = 3): String {
        val latDir = if (latitude < 0) "S" else "N"
        val lngDir = if (longitude < 0) "W" else "E"
        return "${ddmString(latitude, precision)}$latDir    ${
            ddmString(
                longitude,
                precision
            )
        }$lngDir"
    }

    fun toDegreeMinutesSeconds(precision: Int = 1): String {
        val latDir = if (latitude < 0) "S" else "N"
        val lngDir = if (longitude < 0) "W" else "E"
        return "${dmsString(latitude, precision)}${latDir}    ${dmsString(longitude, precision)}${lngDir}"
    }

    fun toMGRS(precision: Int = 5): String {
        return try {
            val lat = Angle.fromDegreesLatitude(latitude)
            val lng = Angle.fromDegreesLongitude(longitude)
            val mgrs = MGRSCoord.fromLatLon(lat, lng, precision)
            mgrs.toString().trim()
        } catch (e: Exception){
            "?"
        }
    }

    fun toUTM(precision: Int = 7): String {
        try {
            val lat = Angle.fromDegreesLatitude(latitude)
            val lng = Angle.fromDegreesLongitude(longitude)
            val utm = UTMCoord.fromLatLon(lat, lng)

            val zone = utm.zone.toString().padStart(2, '0')

            val letter =
                if (latitude < -72) 'C' else if (latitude < -64) 'D' else if (latitude < -56) 'E' else if (latitude < -48) 'F' else if (latitude < -40) 'G' else if (latitude < -32) 'H' else if (latitude < -24) 'J' else if (latitude < -16) 'K' else if (latitude < -8) 'L' else if (latitude < 0) 'M' else if (latitude < 8) 'N' else if (latitude < 16) 'P' else if (latitude < 24) 'Q' else if (latitude < 32) 'R' else if (latitude < 40) 'S' else if (latitude < 48) 'T' else if (latitude < 56) 'U' else if (latitude < 64) 'V' else if (latitude < 72) 'W' else 'X'


            val easting =
                roundUTMPrecision(precision, utm.easting.toInt()).toString().padStart(7, '0') + "E"
            val northing =
                roundUTMPrecision(precision, utm.northing.toInt()).toString().padStart(7, '0') + "N"

            return "$zone$letter $easting $northing"
        } catch (e: Exception) {
            // TODO: Support UPS coordinate system
            return "?"
        }
    }

    private fun roundUTMPrecision(precision: Int, utmValue: Int): Int {
        return (utmValue / power(10.0, 7 - precision)).toInt() * power(10.0, 7 - precision).toInt()
    }

    /**
     * Get the bearing to the other coordinate (using True North)
     */
    fun bearingTo(other: Coordinate): Bearing {
        val results = FloatArray(3)
        Location.distanceBetween(latitude, longitude, other.latitude, other.longitude, results)
        return Bearing(results[1])
    }

    private fun ddmString(degrees: Double, precision: Int = 3): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60).roundPlaces(precision)
        return "$deg°$minutes'"
    }

    private fun dmsString(degrees: Double, precision: Int = 1): String {
        val deg = abs(degrees.toInt())
        val minutes = abs((degrees % 1) * 60)
        val seconds = abs(((minutes % 1) * 60).roundPlaces(precision))
        return "$deg°${minutes.toInt()}'$seconds\""
    }

    companion object {

        val zero = Coordinate(0.0, 0.0)

        fun parse(location: String, format: CoordinateFormat? = null): Coordinate? {
            if (format == null) {
                for (fmt in CoordinateFormat.values()) {
                    val parsed = parse(location, fmt)
                    if (parsed != null) {
                        return parsed
                    }
                }
                return null
            }

            return when (format) {
                CoordinateFormat.DecimalDegrees -> fromDecimalDegrees(location)
                CoordinateFormat.DegreesDecimalMinutes -> fromDegreesDecimalMinutes(location)
                CoordinateFormat.DegreesMinutesSeconds -> fromDegreesMinutesSeconds(location)
                CoordinateFormat.UTM -> fromUTM(location)
                CoordinateFormat.MGRS -> fromMGRS(location)
            }
        }

        private fun fromMGRS(location: String): Coordinate? {
            try {
                val mgrs = MGRSCoord.fromString(location)
                return Coordinate(
                    mgrs.latitude.toDecimalDegreesString(10).replace("°", "").toDouble(),
                    mgrs.longitude.toDecimalDegreesString(10).replace("°", "").toDouble()
                )
            } catch (e: Exception){
                return null
            }
        }

        private fun fromDecimalDegrees(location: String): Coordinate? {
            val regex = Regex("^(-?\\d+(?:\\.\\d+)?)°?[,\\s]+(-?\\d+(?:\\.\\d+)?)°?\$")
            val matches = regex.find(location.trim()) ?: return null
            val latitude = matches.groupValues[1].toDoubleOrNull() ?: return null
            val longitude = matches.groupValues[2].toDoubleOrNull() ?: return null

            if (isValidLatitude(latitude) && isValidLongitude(longitude)) {
                return Coordinate(latitude, longitude)
            }

            return null
        }

        private fun fromDegreesDecimalMinutes(location: String): Coordinate? {
            val ddmRegex = Regex("^(\\d+)°\\s*(\\d+(?:\\.\\d+)?)'\\s*([nNsS])[,\\s]+(\\d+)°\\s*(\\d+(?:\\.\\d+)?)'\\s*([wWeE])\$")
            val matches = ddmRegex.find(location.trim()) ?: return null

            var latitudeDecimal = 0.0
            latitudeDecimal += matches.groupValues[1].toDouble()
            latitudeDecimal += matches.groupValues[2].toDouble() / 60
            latitudeDecimal *= if (matches.groupValues[3].toLowerCase(Locale.getDefault()) == "n") 1 else -1

            var longitudeDecimal = 0.0
            longitudeDecimal += matches.groupValues[4].toDouble()
            longitudeDecimal += matches.groupValues[5].toDouble() / 60
            longitudeDecimal *= if (matches.groupValues[6].toLowerCase(Locale.getDefault()) == "e") 1 else -1

            if (isValidLatitude(latitudeDecimal) && isValidLongitude(longitudeDecimal)) {
                return Coordinate(latitudeDecimal, longitudeDecimal)
            }

            return null
        }

        private fun fromDegreesMinutesSeconds(location: String): Coordinate? {
            val dmsRegex =
                Regex("^(\\d+)°\\s*(\\d+)'\\s*(\\d+(?:\\.\\d+)?)\"\\s*([nNsS])[,\\s]+(\\d+)°\\s*(\\d+)'\\s*(\\d+(?:\\.\\d+)?)\"\\s*([wWeE])\$")
            val matches = dmsRegex.find(location.trim()) ?: return null

            var latitudeDecimal = 0.0
            latitudeDecimal += matches.groupValues[1].toDouble()
            latitudeDecimal += matches.groupValues[2].toDouble() / 60
            latitudeDecimal += matches.groupValues[3].toDouble() / (60 * 60)
            latitudeDecimal *= if (matches.groupValues[4].toLowerCase(Locale.getDefault()) == "n") 1 else -1

            var longitudeDecimal = 0.0
            longitudeDecimal += matches.groupValues[5].toDouble()
            longitudeDecimal += matches.groupValues[6].toDouble() / 60
            longitudeDecimal += matches.groupValues[7].toDouble() / (60 * 60)
            longitudeDecimal *= if (matches.groupValues[8].toLowerCase(Locale.getDefault()) == "e") 1 else -1

            if (isValidLatitude(latitudeDecimal) && isValidLongitude(longitudeDecimal)) {
                return Coordinate(latitudeDecimal, longitudeDecimal)
            }

            return null
        }

        private fun fromUTM(utm: String): Coordinate? {
            val regex =
                Regex("(\\d+)\\s*([c-x,C-X^ioIO])\\s*(\\d+(?:\\.\\d+)?)[\\smMeE]+(\\d+(?:\\.\\d+)?)[\\smMnN]*")
            val matches = regex.find(utm) ?: return null

            val zone = matches.groupValues[1].toInt()
            val letter = matches.groupValues[2].toCharArray().first()
            val easting = matches.groupValues[3].toDouble()
            val northing = matches.groupValues[4].toDouble()

            return fromUTM(zone, letter, easting, northing)
        }

        private fun fromUTM(zone: Int, letter: Char, easting: Double, northing: Double): Coordinate? {
            return try {
                val latLng = UTMCoord.locationFromUTMCoord(
                    zone,
                    if (letter.toUpperCase() <= 'M') AVKey.SOUTH else AVKey.NORTH,
                    easting,
                    northing
                )
                Coordinate(
                    latLng.latitude.toDecimalDegreesString(10).replace("°", "").toDouble(),
                    latLng.longitude.toDecimalDegreesString(10).replace("°", "").toDouble()
                )
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