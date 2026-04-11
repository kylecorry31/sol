package com.kylecorry.sol.units

enum class VolumeUnits(val id: Int, val liters: Double) {
    Liters(1, 1.0),
    Milliliter(2, 0.001),
    USCups(3, 0.236588),
    USPints(4, 0.473176),
    USQuarts(5, 0.946353),
    USOunces(6, 0.0295735),
    USGallons(7, 3.78541),
    ImperialCups(8, 0.284131),
    ImperialPints(9, 0.568261),
    ImperialQuarts(10, 1.13652),
    ImperialOunces(11, 0.0284131),
    ImperialGallons(12, 4.54609),
    USTeaspoons(13, 0.00492892159),
    USTablespoons(14, 0.0147867648),
    ImperialTeaspoons(15, 0.00591939),
    ImperialTablespoons(16, 0.0177582),
}