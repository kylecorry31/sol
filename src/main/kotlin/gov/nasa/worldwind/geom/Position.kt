package gov.nasa.worldwind.geom

/**
 * @author tag
 * @version $Id$
 */
class Position(
    latitude: Angle, longitude: Angle,
    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    val altitude: Double
) : LatLon(latitude, longitude) {
    override fun add(that: Position): Position {
        val lat = Angle.normalizedLatitude(this.latitude.add(that.latitude))
        val lon = Angle.normalizedLongitude(this.longitude.add(that.longitude))

        return Position(lat, lon, this.altitude + that.altitude)
    }

    override fun subtract(that: Position): Position {
        val lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude))
        val lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude))

        return Position(lat, lon, this.altitude - that.altitude)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false
        if (!super.equals(other)) return false

        return other.altitude == this.altitude
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        val temp: Long = if (this.altitude != 0.0) this.altitude.toBits() else 0L
        result = 31 * result + (temp xor (temp ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "(" + this.latitude.toString() + ", " + this.longitude.toString() + ", " + this.altitude + ")"
    }

    companion object {
        val ZERO: Position = Position(Angle.ZERO, Angle.ZERO, 0.0)
    }
}