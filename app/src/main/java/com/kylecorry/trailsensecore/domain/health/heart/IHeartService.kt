package com.kylecorry.trailsensecore.domain.health.heart

interface IHeartService {
    fun classifyBloodPressure(pressure: BloodPressure): BloodPressureCategory
    fun classifyPulseOxygen(percent: Float): PulseOxygenCategory
}