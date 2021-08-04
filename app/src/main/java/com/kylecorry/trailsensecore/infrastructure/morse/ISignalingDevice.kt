package com.kylecorry.trailsensecore.infrastructure.morse

interface ISignalingDevice {
    fun on()
    fun off()

    companion object {
        fun from(turnOn: () -> Unit, turnOff: () -> Unit): ISignalingDevice {
            return object : ISignalingDevice {
                override fun on() {
                    turnOn()
                }

                override fun off() {
                    turnOff()
                }
            }
        }
    }
}