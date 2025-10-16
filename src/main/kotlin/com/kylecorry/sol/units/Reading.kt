package com.kylecorry.sol.units

import kotlinx.datetime.Instant

data class Reading<T>(val value: T, val time: Instant)