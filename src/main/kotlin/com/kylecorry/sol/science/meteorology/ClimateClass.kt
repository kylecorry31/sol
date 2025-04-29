package com.kylecorry.sol.science.meteorology

data class KoppenGeigerClimateClassification(
    val climateGroup: KoppenGeigerClimateGroup,
    val seasonalPrecipitationPattern: KoppenGeigerSeasonalPrecipitationPattern?,
    val temperaturePattern: KoppenGeigerTemperaturePattern?,
)

enum class KoppenGeigerClimateGroup {
    Tropical,
    Dry,
    Temperate,
    Continental,
    Polar
}

enum class KoppenGeigerSeasonalPrecipitationPattern {
    Rainforest,
    Monsoon,
    Savanna,
    Desert,
    Steppe,
    DrySummer,
    DryWinter,
    NoDrySeason,
    Tundra,
    IceCap
}

enum class KoppenGeigerTemperaturePattern {
    Hot,
    Cold,
    HotSummer,
    WarmSummer,
    ColdSummer,
    VeryColdWinter
}