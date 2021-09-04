package com.kylecorry.trailsensecore.science.astronomy.moon

private const val truePhaseWidth = 11.25f

enum class MoonTruePhase(val startAngle: Float, val endAngle: Float) {
    New(360 - truePhaseWidth, truePhaseWidth),
    WaningCrescent(truePhaseWidth, 90 - truePhaseWidth),
    ThirdQuarter(90 - truePhaseWidth, 90 + truePhaseWidth),
    WaningGibbous(90 + truePhaseWidth, 180 - truePhaseWidth),
    Full(180 - truePhaseWidth, 180 + truePhaseWidth),
    WaxingGibbous(180 + truePhaseWidth, 270 - truePhaseWidth),
    FirstQuarter(270 - truePhaseWidth, 270 + truePhaseWidth),
    WaxingCrescent(270 + truePhaseWidth, 360 - truePhaseWidth)
}