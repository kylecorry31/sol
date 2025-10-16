/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.dstl.geo.osgb

import kotlin.math.*

/** Convert National grid (TM 123412 23434) to OSGB Northing and Easting. */
object NationalGrid {
    private const val SIZE_M = 100000.0

    private class GridSquare(
        val reference: String,
        val easting: Double,
        val northing: Double
    ) {
        /**
         * Is the given Easting-Northing pair inside this grid square?
         *
         * @param en An array of Easting-Northing coordinates (in that order)
         */
        fun inside(en: DoubleArray): Boolean = inside(en[0], en[1])

        /**
         * Is the given Easting-Northing pair inside this grid square?
         *
         * @param e Easting
         * @param n Northing
         */
        fun inside(e: Double, n: Double): Boolean {
            return northing <= n && n < northing + SIZE_M && easting <= e && e < easting + SIZE_M
        }

        /**
         * Return the offset of the provided absolute Easting-Northing within this grid square
         *
         * @param en An array of Easting-Northing coordinates (in that order)
         */
        fun offsetEastingNorthing(en: DoubleArray): DoubleArray = offsetEastingNorthing(en[0], en[1])

        /**
         * Return the offset of the provided absolute Easting-Northing within this grid square
         *
         * @param e Easting
         * @param n Northing
         */
        fun offsetEastingNorthing(e: Double, n: Double): DoubleArray {
            return doubleArrayOf(e - easting, n - northing)
        }

        /**
         * Convert an Easting-Northing from a relative (to this grid square) pair to an absolute pair
         *
         * @param e Easting
         * @param n Northing
         */
        fun toEastingNorthing(e: Double, n: Double): DoubleArray {
            return doubleArrayOf(e + easting, n + northing)
        }
    }

    private val GRID_SQUARES: Map<String, GridSquare>

    init {
        val squares = mutableMapOf<String, GridSquare>()

        fun addGridSquare(reference: String, x: Int, y: Int) {
            squares[reference] = GridSquare(reference, x * SIZE_M, y * SIZE_M)
        }

        addGridSquare("SV", 0, 0)
        addGridSquare("SW", 1, 0)
        addGridSquare("SX", 2, 0)
        addGridSquare("SY", 3, 0)
        addGridSquare("SZ", 4, 0)
        addGridSquare("TV", 5, 0)
        addGridSquare("TW", 6, 0)

        addGridSquare("SQ", 0, 1)
        addGridSquare("SR", 1, 1)
        addGridSquare("SS", 2, 1)
        addGridSquare("ST", 3, 1)
        addGridSquare("SU", 4, 1)
        addGridSquare("TQ", 5, 1)
        addGridSquare("TR", 6, 1)

        addGridSquare("SL", 0, 2)
        addGridSquare("SM", 1, 2)
        addGridSquare("SN", 2, 2)
        addGridSquare("SO", 3, 2)
        addGridSquare("SP", 4, 2)
        addGridSquare("TL", 5, 2)
        addGridSquare("TM", 6, 2)

        addGridSquare("SF", 0, 3)
        addGridSquare("SG", 1, 3)
        addGridSquare("SH", 2, 3)
        addGridSquare("SJ", 3, 3)
        addGridSquare("SK", 4, 3)
        addGridSquare("TF", 5, 3)
        addGridSquare("TG", 6, 3)

        addGridSquare("SA", 0, 4)
        addGridSquare("SB", 1, 4)
        addGridSquare("SC", 2, 4)
        addGridSquare("SD", 3, 4)
        addGridSquare("SE", 4, 4)
        addGridSquare("TA", 5, 4)
        addGridSquare("TB", 6, 4)

        // n AND o

        addGridSquare("NV", 0, 5)
        addGridSquare("NW", 1, 5)
        addGridSquare("NX", 2, 5)
        addGridSquare("NY", 3, 5)
        addGridSquare("NZ", 4, 5)
        addGridSquare("OV", 5, 5)
        addGridSquare("OW", 6, 5)

        addGridSquare("NQ", 0, 6)
        addGridSquare("NR", 1, 6)
        addGridSquare("NS", 2, 6)
        addGridSquare("NT", 3, 6)
        addGridSquare("NU", 4, 6)
        addGridSquare("OQ", 5, 6)
        addGridSquare("OR", 6, 6)

        addGridSquare("NL", 0, 7)
        addGridSquare("NM", 1, 7)
        addGridSquare("NN", 2, 7)
        addGridSquare("NO", 3, 7)
        addGridSquare("NP", 4, 7)
        addGridSquare("OL", 5, 7)
        addGridSquare("OM", 6, 7)

        addGridSquare("NF", 0, 8)
        addGridSquare("NG", 1, 8)
        addGridSquare("NH", 2, 8)
        addGridSquare("NJ", 3, 8)
        addGridSquare("NK", 4, 8)
        addGridSquare("OF", 5, 8)
        addGridSquare("OG", 6, 8)

        addGridSquare("NA", 0, 9)
        addGridSquare("NB", 1, 9)
        addGridSquare("NC", 2, 9)
        addGridSquare("ND", 3, 9)
        addGridSquare("NE", 4, 9)
        addGridSquare("OA", 5, 9)
        addGridSquare("OB", 6, 9)

        // h & j

        addGridSquare("HV", 0, 10)
        addGridSquare("HW", 1, 10)
        addGridSquare("HX", 2, 10)
        addGridSquare("HY", 3, 10)
        addGridSquare("HZ", 4, 10)
        addGridSquare("JV", 5, 10)
        addGridSquare("JW", 6, 10)

        addGridSquare("HQ", 0, 11)
        addGridSquare("HR", 1, 11)
        addGridSquare("HS", 2, 11)
        addGridSquare("HT", 3, 11)
        addGridSquare("HU", 4, 11)
        addGridSquare("JQ", 5, 11)
        addGridSquare("JR", 6, 11)

        addGridSquare("HL", 0, 12)
        addGridSquare("HM", 1, 12)
        addGridSquare("HN", 2, 12)
        addGridSquare("HO", 3, 12)
        addGridSquare("HP", 4, 12)
        addGridSquare("JL", 5, 12)
        addGridSquare("JM", 6, 12)

        GRID_SQUARES = squares
    }

    /**
     * Convert from a string to northing and easting
     *
     * @param ng the national grid string
     * @return array [n,e]
     */
    fun fromNationalGrid(ng: String): DoubleArray {
        val trimmed = ng.trim()
        val ref = trimmed.substring(0, 2)

        val gridSquare = GRID_SQUARES[ref]
            ?: throw IllegalArgumentException("Invalid NG: $trimmed")

        val list = splitOnWhitespace(trimmed.substring(2))

        val n: Double
        val e: Double
        when {
            list.size >= 2 -> {
                // Have two values so use them
                e = parseDoubleWithCoordPrecision(list[0])
                    ?: throw IllegalArgumentException("Unable to extract NE from $ng")
                n = parseDoubleWithCoordPrecision(list[1])
                    ?: throw IllegalArgumentException("Unable to extract NE from $ng")
            }
            list.size == 1 -> {
                // Consolidated value
                val ret = splitConsolidated(list[0])
                e = parseDoubleWithCoordPrecision(ret[0])
                    ?: throw IllegalArgumentException("Unable to extract NE from $ng")
                n = parseDoubleWithCoordPrecision(ret[1])
                    ?: throw IllegalArgumentException("Unable to extract NE from $ng")
            }
            else -> throw IllegalArgumentException("Invalid NG coords $ng")
        }

        return gridSquare.toEastingNorthing(e, n)
    }

    /**
     * Split a string into two Strings
     *
     * @param s The string to split
     * @return An array, containing the Easting and Northing (in that order) split from a string
     */
    private fun splitConsolidated(s: String): Array<String> {
        if (s.length % 2 != 0) {
            throw IllegalArgumentException(
                "Differing size of northing and easting, unable to determine valid ref $s"
            )
        }

        val index = s.length / 2
        return arrayOf(
            s.substring(0, index), // Easting
            s.substring(index) // Northing
        )
    }

    private fun parseDoubleWithCoordPrecision(s: String): Double? {
        var precedingZeroes = 0
        var t = s
        while (t.startsWith("0")) {
            precedingZeroes++
            t = t.substring(1)
        }

        val c: Double = try {
            t.toDouble()
        } catch (nfe: NumberFormatException) {
            return null
        }

        val multiplier = 10.0.pow(4 - precedingZeroes - floor(log10(c)))
        return c * multiplier
    }

    /**
     * Convert EastingsNorthings to a NationalGrid reference.
     *
     * <p>This is not a very optimised implementation.
     *
     * @param en array of {e,n}
     * @return optional empty if the ne has no representation
     */
    fun toNationalGrid(en: DoubleArray): String? {
        for (gs in GRID_SQUARES.values) {
            if (gs.inside(en)) {
                val offset = gs.offsetEastingNorthing(en)
                return String.format("%s %05.0f %05.0f", gs.reference, offset[0], offset[1])
            }
        }
        return null
    }

    internal fun splitOnWhitespace(s: String): List<String> {
        return s.split(Regex("\\h+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }
}
