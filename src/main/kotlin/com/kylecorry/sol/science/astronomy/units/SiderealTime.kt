package com.kylecorry.sol.science.astronomy.units

import com.kylecorry.sol.math.arithmetic.Arithmetic.wrap

internal open class SiderealTime(_hours: Double, val longitude: Double) {

    val hours = wrap(_hours, 0.0, 24.0)

    fun atGreenwich(): GreenwichSiderealTime {
        return GreenwichSiderealTime(hours - longitude / 15)
    }

    fun atLongitude(longitude: Double): SiderealTime {
        return SiderealTime(hours + (longitude - this.longitude) / 15, longitude)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SiderealTime

        if (longitude != other.longitude) return false
        if (hours != other.hours) return false

        return true
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + hours.hashCode()
        return result
    }


}