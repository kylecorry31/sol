package gov.nasa.worldwind.geom

/**
 * Represents a point on the two-dimensional surface of a globe. Latitude is the degrees North and ranges between [-90,
 * 90], while longitude refers to degrees East, and ranges between (-180, 180].
 *
 *
 * Instances of `LatLon` are immutable.
 *
 * @author Tom Gaskins
 * @version $Id$
 */
open class LatLon {
    /**
     * Obtains the latitude of this `LatLon`.
     *
     * @return this `LatLon`'s latitude
     */
    val latitude: Angle

    /**
     * Obtains the longitude of this `LatLon`.
     *
     * @return this `LatLon`'s longitude
     */
    val longitude: Angle

    /**
     * Constructs a new  `LatLon` from two angles. Neither angle may be null.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @throws IllegalArgumentException if `latitude` or `longitude` is null
     */
    constructor(latitude: Angle, longitude: Angle) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun add(that: LatLon): LatLon {

        val lat = Angle.normalizedLatitude(this.latitude.add(that.latitude))
        val lon = Angle.normalizedLongitude(this.longitude.add(that.longitude))

        return LatLon(lat, lon)
    }

    fun subtract(that: LatLon): LatLon {

        val lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude))
        val lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude))

        return LatLon(lat, lon)
    }

    override fun toString(): String {
        val las = String.format("Lat %7.4f\u00B0", this.latitude.degrees)
        val los = String.format("Lon %7.4f\u00B0", this.longitude.degrees)
        return "($las, $los)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LatLon) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int = latitude.hashCode()
        result = 29 * result + longitude.hashCode()
        return result
    }

    companion object {
        val ZERO: LatLon = LatLon(Angle.ZERO, Angle.ZERO)
    }
}