package com.kylecorry.trailsensecore.domain.astronomy.sun

internal class CivilTwilightCalculator : BaseSunTimesCalculator(-6f)
internal class NauticalTwilightCalculator : BaseSunTimesCalculator(-12f)
internal class AstronomicalTwilightCalculator : BaseSunTimesCalculator(-18f)
internal class ActualTwilightCalculator : BaseSunTimesCalculator(-0.833f)
