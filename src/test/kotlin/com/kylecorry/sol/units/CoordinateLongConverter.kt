package com.kylecorry.sol.units

import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.SimpleArgumentConverter

class CoordinateLongConverter : SimpleArgumentConverter() {
    override fun convert(source: Any?, targetType: Class<*>): Any {
        if (source == null) {
            throw ArgumentConversionException("Source is null")
        }

        return when (targetType) {
            Long::class.java -> {
                when (source) {
                    is Coordinate -> source.packed
                    is Number -> source.toLong()
                    else -> throw ArgumentConversionException("Cannot convert ${source::class.java} to packed Coordinate long")
                }
            }

            else -> throw ArgumentConversionException("Unsupported target type: ${targetType.name}")
        }
    }
}
