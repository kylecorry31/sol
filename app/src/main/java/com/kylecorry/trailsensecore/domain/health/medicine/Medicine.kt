package com.kylecorry.trailsensecore.domain.health.medicine

data class Medicine(val id: Long, val name: String, val frequency: Frequency, val isReminding: Boolean = false, val dosage: String?, val foodRequirement: MedicineFoodRequirement?)
