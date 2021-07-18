package com.kylecorry.trailsensecore.domain.packs.sort

import com.kylecorry.trailsensecore.domain.packs.PackItem

interface IPackItemSort {

    fun sort(items: List<PackItem>): List<PackItem>

}