package com.kylecorry.trailsensecore.domain.health.virus

import java.time.Instant

data class Contact(val id: Long, val name: String, val lastContacted: Instant)
