package com.kylecorry.trailsensecore.infrastructure.sensors.microphone

import com.kylecorry.trailsensecore.infrastructure.sensors.ISensor

interface IMicrophone: ISensor {
    val audio: ShortArray?
    val amplitude: Short?
}