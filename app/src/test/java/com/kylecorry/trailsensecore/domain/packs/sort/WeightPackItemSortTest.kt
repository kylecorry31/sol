package com.kylecorry.trailsensecore.domain.packs.sort

import com.kylecorry.trailsensecore.domain.packs.ItemCategory
import com.kylecorry.trailsensecore.domain.packs.PackItem
import com.kylecorry.trailsensecore.domain.units.Weight
import com.kylecorry.trailsensecore.domain.units.WeightUnits
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class WeightPackItemSortTest {

    @Test
    fun sortAsc() {
        val items = listOf(
            item(0, "Test", ItemCategory.Natural, 0.0, Weight(10f, WeightUnits.Kilograms)),
            item(1, "Test 1", ItemCategory.Other, 1.0, null),
            item(2, "Test 2", ItemCategory.Other, 3.0, Weight(1f, WeightUnits.Grams)),
            item(3, "Something", ItemCategory.Electronics, 0.0, null),
            item(4, "Test 1", ItemCategory.Other, 1.0, Weight(11f, WeightUnits.Kilograms)),
            item(5, "Test 1", ItemCategory.Other, 1.0, Weight(9f, WeightUnits.Kilograms)),
            item(6, "Test", ItemCategory.Other, 2.0, Weight(11f, WeightUnits.Kilograms)),
            item(7, "Test", ItemCategory.Other, 1.0, Weight(11f, WeightUnits.Kilograms)),
        )

        val sort = WeightPackItemSort(true)
        val expected = listOf<Long>(0, 2, 5, 7, 4, 6, 3, 1)

        val sorted = sort.sort(items).map { it.id }

        assertEquals(expected, sorted)
    }

    @Test
    fun sortDesc() {
        val items = listOf(
            item(0, "Test", ItemCategory.Natural, 0.0, Weight(10f, WeightUnits.Kilograms)),
            item(1, "Test 1", ItemCategory.Other, 1.0, null),
            item(2, "Test 2", ItemCategory.Other, 3.0, Weight(1f, WeightUnits.Grams)),
            item(3, "Something", ItemCategory.Electronics, 0.0, null),
            item(4, "Test 1", ItemCategory.Other, 1.0, Weight(11f, WeightUnits.Kilograms)),
            item(5, "Test 1", ItemCategory.Other, 1.0, Weight(9f, WeightUnits.Kilograms)),
            item(6, "Test", ItemCategory.Other, 2.0, Weight(11f, WeightUnits.Kilograms)),
            item(7, "Test", ItemCategory.Other, 1.0, Weight(11f, WeightUnits.Kilograms)),
        )

        val sort = WeightPackItemSort(false)
        val expected = listOf<Long>(6, 7, 4, 5, 2, 0, 3, 1)

        val sorted = sort.sort(items).map { it.id }

        assertEquals(expected, sorted)
    }

    private fun item(
        id: Long,
        name: String,
        category: ItemCategory,
        amount: Double,
        weight: Weight? = null
    ): PackItem {
        return PackItem(id, 0, name, category, amount, weight = weight)
    }
}