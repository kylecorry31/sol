package com.kylecorry.sol.units

import java.time.Instant

data class Reading<T>(val value: T, val time: Instant)