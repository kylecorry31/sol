package com.kylecorry.sol.science.meteorology.clouds

enum class CloudGenus(val id: Long, val level: CloudLevel, val categories: Array<CloudCategory>) {
    // https://www.weather.gov/jetstream/basicten
    Cirrus(1, CloudLevel.High, arrayOf(CloudCategory.Cirro)),
    Cirrocumulus(2, CloudLevel.High, arrayOf(CloudCategory.Cirro, CloudCategory.Cumulo)),
    Cirrostratus(3, CloudLevel.High, arrayOf(CloudCategory.Cirro, CloudCategory.Strato)),
    Altocumulus(4, CloudLevel.Mid, arrayOf(CloudCategory.Cumulo)),
    Altostratus(5, CloudLevel.Mid, arrayOf(CloudCategory.Strato)),
    Nimbostratus(6, CloudLevel.Mid, arrayOf(CloudCategory.Strato, CloudCategory.Nimbo)),
    Stratus(7, CloudLevel.Low, arrayOf(CloudCategory.Strato)),
    Stratocumulus(8, CloudLevel.Low, arrayOf(CloudCategory.Strato, CloudCategory.Cumulo)),
    Cumulus(9, CloudLevel.Low, arrayOf(CloudCategory.Cumulo)),
    Cumulonimbus(10, CloudLevel.Low, arrayOf(CloudCategory.Cumulo, CloudCategory.Nimbo))
}