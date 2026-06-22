package com.kylecorry.sol.science.astronomy.moon

/**
 * Details about the moon phase
 * @param phase the nearest true phase
 * @param illumination the percent of the near side of the moon that is illuminated between 0 and 100
 * @param phaseAngle the phase angle in degrees where 0 = new, 90 = first quarter, 180 = full, 270 = last quarter
 * @param lunarAge the fractional days since the new moon
 */
data class MoonPhase(val phase: MoonTruePhase, val illumination: Float, val phaseAngle: Float, val lunarAge: Float)
