package com.kylecorry.trailsensecore.infrastructure.text

import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap

object DecimalFormatter {

    private val formatterMap = ConcurrentHashMap<Int, DecimalFormat>()

    fun format(number: Number, decimalPlaces: Int): String {
        val existing = formatterMap[decimalPlaces]
        if (existing != null){
            return existing.format(number)
        }
        if (decimalPlaces <= 0){
            val formatter = DecimalFormat("#")
            formatterMap.putIfAbsent(0, formatter)
            return formatter.format(number)
        }

        val builder = StringBuilder("#.")
        for (i in 0 until decimalPlaces){
            builder.append('#')
        }

        val fmt = DecimalFormat(builder.toString())
        formatterMap.putIfAbsent(decimalPlaces, fmt)
        return fmt.format(number)
    }
}