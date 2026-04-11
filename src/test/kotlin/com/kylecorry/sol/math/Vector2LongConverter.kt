package com.kylecorry.sol.math

import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.SimpleArgumentConverter

/**
 * Converts between Vector2 and Long (packed) for parameterized tests.
 * - If target type is Vector2, accepts Long (packed) and constructs Vector2.
 * - If target type is Long, accepts Vector2 and returns its packed value.
 */
class Vector2LongConverter : SimpleArgumentConverter() {
    override fun convert(source: Any?, targetType: Class<*>): Any {
        if (source == null) {
            throw ArgumentConversionException("Source is null")
        }

        return when (targetType) {
            List::class.java -> {
                if (source is java.util.List<*>) {
                    source.map { item ->
                        when (item) {
                            is Long -> Vector2(item)
                            is Number -> Vector2(item.toLong())
                            is Vector2 -> item
                            else -> throw ArgumentConversionException("Cannot convert list element ${item?.let { it::class.java }} to Vector2")
                        }
                    }
                } else {
                    throw ArgumentConversionException("Cannot convert ${source::class.java} to List<Vector2>. Expected List<Long|Number|Vector2>.")
                }
            }

            Long::class.java -> {
                if (source is Vector2) {
                    source.packed
                } else {
                    throw ArgumentConversionException("Cannot convert ${source::class.java} to Long. Expected Vector2 -> Long.")
                }
            }

            else -> throw ArgumentConversionException("Unsupported target type: ${targetType.name}")
        }
    }
}
