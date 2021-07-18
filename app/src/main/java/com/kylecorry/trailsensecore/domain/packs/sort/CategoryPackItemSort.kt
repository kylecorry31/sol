package com.kylecorry.trailsensecore.domain.packs.sort

import com.kylecorry.trailsensecore.domain.packs.PackItem

class CategoryPackItemSort : IPackItemSort {
    override fun sort(items: List<PackItem>): List<PackItem> {
        return items.sortedWith(
            compareBy(
                { it.category.name },
                { it.name },
                { it.id }
            )
        )
    }
}