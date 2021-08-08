package com.kylecorry.trailsensecore.infrastructure.sensors.microphone

import com.kylecorry.sense.ISensor

interface IMicrophone: ISensor {
    val audio: ShortArray?
    val amplitude: Short?
}