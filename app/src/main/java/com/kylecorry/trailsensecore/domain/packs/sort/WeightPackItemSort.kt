package com.kylecorry.trailsensecore.domain.packs.sort

import com.kylecorry.trailsensecore.domain.packs.PackItem
import com.kylecorry.trailsensecore.domain.units.WeightUnits

class WeightPackItemSort(private val ascending: Boolean = true) : IPackItemSort {
    override fun sort(items: List<PackItem>): List<PackItem> {
        return items.sortedWith(
            compareBy(
                {
                    val weight = it.packedWeight?.convertTo(WeightUnits.Grams)?.weight
                    val defaultWeight = if (ascending) Float.POSITIVE_INFINITY else 0f
                    val sortableWeight = weight ?: defaultWeight
                    if (ascending) sortableWeight else -sortableWeight
                },
                { it.category.name },
                { it.name },
                { it.id }
            )
        )
    }
}