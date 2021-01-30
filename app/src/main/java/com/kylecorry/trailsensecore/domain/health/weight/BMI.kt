package com.kylecorry.trailsensecore.domain.health.weight

data class BMI(val value: Float) {

    val weightStatus: WeightStatus
        get() {
            return when {
                value < 18.5 -> WeightStatus.Underweight
                value < 25 -> WeightStatus.Normal
                value < 30 -> WeightStatus.Overweight
                value < 35 -> WeightStatus.Obese1
                value < 40 -> WeightStatus.Obese2
                else -> WeightStatus.Obese3
            }
        }

}
