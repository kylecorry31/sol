package com.kylecorry.trailsensecore.domain.astronomy.eclipse

import com.kylecorry.trailsensecore.domain.astronomy.Astro
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class LunarEclipseParameterProvider {

    fun getNextLunarEclipseParameters(after: Instant): LunarEclipseParameters {
        val ut = ZonedDateTime.ofInstant(after, ZoneId.of("UTC")).toLocalDateTime()
        return Astro.getNextLunarEclipse(ut)
    }

}