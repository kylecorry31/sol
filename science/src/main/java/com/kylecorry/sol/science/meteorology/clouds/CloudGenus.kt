package com.kylecorry.sol.science.meteorology.clouds

enum class CloudGenus(val level: CloudLevel, val categories: Array<CloudCategory>) {
    // https://www.weather.gov/jetstream/basicten
    Cirrus(CloudLevel.High, arrayOf(CloudCategory.Cirro)),
    Cirrocumulus(CloudLevel.High, arrayOf(CloudCategory.Cirro, CloudCategory.Cumulo)),
    Cirrostratus(CloudLevel.High, arrayOf(CloudCategory.Cirro, CloudCategory.Strato)),
    Altocumulus(CloudLevel.Mid, arrayOf(CloudCategory.Cumulo)),
    Altostratus(CloudLevel.Mid, arrayOf(CloudCategory.Strato)),
    Nimbostratus(CloudLevel.Mid, arrayOf(CloudCategory.Strato, CloudCategory.Nimbo)),
    Stratus(CloudLevel.Low, arrayOf(CloudCategory.Strato)),
    Stratocumulus(CloudLevel.Low, arrayOf(CloudCategory.Strato, CloudCategory.Cumulo)),
    Cumulus(CloudLevel.Low, arrayOf(CloudCategory.Cumulo)),
    Cumulonimbus(CloudLevel.Low, arrayOf(CloudCategory.Cumulo, CloudCategory.Nimbo))
}