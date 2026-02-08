package com.kylecorry.sol.science.ecology

import com.kylecorry.sol.science.ecology.triggers.LifecycleEventTrigger
import java.time.Duration

data class LifecycleEvent(val name: String, val trigger: LifecycleEventTrigger, val offset: Duration? = null)
