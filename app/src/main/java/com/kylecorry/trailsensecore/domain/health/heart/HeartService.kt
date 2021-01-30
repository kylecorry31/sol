package com.kylecorry.trailsensecore.domain.health.heart

class HeartService : IHeartService {

    override fun classifyBloodPressure(pressure: BloodPressure): BloodPressureCategory {
        if (pressure.systolic < 90 || pressure.diastolic < 60) {
            return BloodPressureCategory.Hypotension
        }

        if (pressure.systolic < 120 && pressure.diastolic < 80) {
            return BloodPressureCategory.Normal
        }

        if (pressure.systolic in 120..129 && pressure.diastolic < 80) {
            return BloodPressureCategory.Elevated
        }

        if (pressure.systolic in 130..139 || pressure.diastolic in 80..89) {
            return BloodPressureCategory.Hypertension1
        }

        if (pressure.systolic > 180 || pressure.diastolic > 120) {
            return BloodPressureCategory.HypertensiveCrisis
        }

        return BloodPressureCategory.Hypertension2
    }

    override fun classifyPulseOxygen(percent: Float): PulseOxygenCategory {
        return if (percent < 95) PulseOxygenCategory.Low else PulseOxygenCategory.Normal
    }

}