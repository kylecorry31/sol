package com.kylecorry.sol.science.geography.formatting

import com.kylecorry.sol.shared.toDoubleCompat
import com.kylecorry.sol.units.Coordinate
import uk.gov.dstl.geo.osgb.Constants
import uk.gov.dstl.geo.osgb.EastingNorthingConversion
import uk.gov.dstl.geo.osgb.NationalGrid
import uk.gov.dstl.geo.osgb.OSGB36

class OSGBCoordinateFormat(private val precision: Int = 5) : CoordinateFormat {
    override fun toString(coordinate: Coordinate): String {
        try {
            val osgb36 = OSGB36.fromWGS84(coordinate.latitude, coordinate.longitude)
            val en = EastingNorthingConversion.fromLatLon(
                osgb36,
                Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
                Constants.ELLIPSOID_AIRY1830_MINORAXIS,
                Constants.NATIONALGRID_N0,
                Constants.NATIONALGRID_E0,
                Constants.NATIONALGRID_F0,
                Constants.NATIONALGRID_LAT0,
                Constants.NATIONALGRID_LON0
            )
            val ng = NationalGrid.toNationalGrid(en)
            if (ng != null) {
                return ng
            }
        } catch (e: Exception) {
            return "?"
        }
        return "?"
    }

    override fun parse(text: String): Coordinate? {
        return try {
            val eastingNorthing = try {
                NationalGrid.fromNationalGrid(text)
            } catch (e: Exception) {
                val split = text.split(",")
                val en = split.mapNotNull { it.toDoubleCompat() }.toDoubleArray()
                NationalGrid.toNationalGrid(en)
                en
            }
            val latlonOSGB = EastingNorthingConversion.toLatLon(
                eastingNorthing,
                Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
                Constants.ELLIPSOID_AIRY1830_MINORAXIS,
                Constants.NATIONALGRID_N0,
                Constants.NATIONALGRID_E0,
                Constants.NATIONALGRID_F0,
                Constants.NATIONALGRID_LAT0,
                Constants.NATIONALGRID_LON0
            )
            val latlonWGS84 = OSGB36.toWGS84(latlonOSGB[0], latlonOSGB[1])
            return Coordinate(latlonWGS84[0], latlonWGS84[1])
        } catch (e: Exception) {
            null
        }
    }
}
