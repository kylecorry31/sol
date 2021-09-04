package com.kylecorry.trailsensecore.science.astronomy.units

import com.kylecorry.andromeda.core.math.wrap

class EquatorialCoordinate(
    _declination: Double,
    _rightAscension: Double,
    val isApparent: Boolean = false
) {
    val declination = wrap(_declination, -90.0, 90.0)
    val rightAscension = wrap(_rightAscension, 0.0, 360.0)

    val rightAscensionHours = rightAscension / 15

    fun getHourAngle(siderealTime: SiderealTime): Double {
        return wrap(siderealTime.hours - rightAscensionHours, 0.0, 24.0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EquatorialCoordinate

        if (declination != other.declination) return false
        if (rightAscension != other.rightAscension) return false

        return true
    }

    override fun hashCode(): Int {
        var result = declination.hashCode()
        result = 31 * result + rightAscension.hashCode()
        return result
    }

    companion object {
        fun fromHourAngle(
            declination: Double,
            hourAngle: Double,
            siderealTime: SiderealTime
        ): EquatorialCoordinate {
            return fromRightAscensionHours(
                declination,
                siderealTime.hours - hourAngle
            )
        }

        fun fromRightAscensionHours(
            declination: Double,
            rightAscensionHours: Double
        ): EquatorialCoordinate {
            return EquatorialCoordinate(declination, rightAscensionHours * 15)
        }
    }

}