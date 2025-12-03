package com.kylecorry.sol.units

import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.SimpleArgumentConverter

/**
 * Converts between Coordinate and Long (packed) for parameterized tests.
 * - If target type is Coordinate, accepts Long/Number and constructs Coordinate from packed bits.
 * - If target type is Long, accepts Coordinate and returns its packed long representation.
 */
class CoordinateLongConverter : SimpleArgumentConverter() {
    override fun convert(source: Any?, targetType: Class<*>): Any? {
        if (source == null) {
            return null
        }

        return when (targetType) {
            Coordinate::class.java -> {
                when (source) {
                    is Long -> Coordinate(source)
                    is Number -> Coordinate(source.toLong())
                    is Coordinate -> source
                    else -> throw ArgumentConversionException("Cannot convert ${source::class.java} to Coordinate. Expected Long|Number|Coordinate.")
                }
            }

            Long::class.java -> {
                if (source is Coordinate) {
                    source.bits
                } else {
                    throw ArgumentConversionException("Cannot convert ${source::class.java} to Long. Expected Coordinate -> Long.")
                }
            }

            else -> throw ArgumentConversionException("Unsupported target type: ${targetType.name}")
        }
    }
}
