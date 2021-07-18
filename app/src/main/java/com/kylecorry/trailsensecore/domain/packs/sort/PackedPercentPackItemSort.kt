package com.kylecorry.trailsensecore.domain.packs.sort

import com.kylecorry.trailsensecore.domain.packs.PackItem

class PackedPercentPackItemSort(private val ascending: Boolean = true) : IPackItemSort {
    override fun sort(items: List<PackItem>): List<PackItem> {
        return items.sortedWith(
            compareBy(
                { if (ascending) it.percentPacked else -it.percentPacked },
                { it.category.name },
                { it.name },
                { it.id }
            )
        )
    }
}