package com.kylecorry.trailsensecore.domain.units

enum class VolumeUnits(val liters: Float) {
    Liters(1f),
    Milliliter(0.001f),
    USOunces(0.0295735f),
    USGallons(3.78541f),
    ImperialOunces(0.0284131f),
    ImperialGallons(4.54609f)
}