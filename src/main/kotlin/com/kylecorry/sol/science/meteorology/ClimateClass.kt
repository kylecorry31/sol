package com.kylecorry.sol.science.meteorology

data class KoppenGeigerClimateClassification(
    val climateGroup: KoppenGeigerClimateGroup,
    val seasonalPrecipitationPattern: KoppenGeigerSeasonalPrecipitationPattern?,
    val temperaturePattern: KoppenGeigerTemperaturePattern?,
) {
    val code: String =
        "${climateGroup.code}${seasonalPrecipitationPattern?.code ?: ""}${temperaturePattern?.code ?: ""}"
}

enum class KoppenGeigerClimateGroup(val code: Char) {
    Tropical('A'),
    Dry('B'),
    Temperate('C'),
    Continental('D'),
    Polar('E'),
}

enum class KoppenGeigerSeasonalPrecipitationPattern(val code: Char) {
    Rainforest('f'),
    Monsoon('m'),
    Savanna('s'),
    Desert('W'),
    Steppe('S'),
    DrySummer('s'),
    DryWinter('w'),
    NoDrySeason('f'),
    Tundra('T'),
    IceCap('F'),
}

enum class KoppenGeigerTemperaturePattern(val code: Char) {
    Hot('h'),
    Cold('k'),
    HotSummer('a'),
    WarmSummer('b'),
    ColdSummer('c'),
    VeryColdWinter('d'),
}