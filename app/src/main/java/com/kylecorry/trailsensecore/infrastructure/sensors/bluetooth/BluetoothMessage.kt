package com.kylecorry.trailsensecore.infrastructure.sensors.bluetooth

import java.time.Instant

data class BluetoothMessage(val message: String, val timestamp: Instant)
