package com.kylecorry.trailsensecore.domain.health.medicine

import java.time.DayOfWeek

data class Frequency(val days: List<DayOfWeek>, val times: List<TimeOfDay>)
