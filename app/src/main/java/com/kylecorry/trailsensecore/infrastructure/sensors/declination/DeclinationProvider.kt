package com.kylecorry.trailsensecore.infrastructure.sensors.declination

import com.kylecorry.trailsensecore.domain.navigation.DeclinationCalculator
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.trailsensecore.infrastructure.sensors.gps.IGPS

class DeclinationProvider(private val gps: IGPS, private val altimeter: IAltimeter) :
    AbstractSensor(), IDeclinationProvider {
    override val declination: Float
        get() = _declination

    override val hasValidReading: Boolean
        get() = gotLocation && gotAltitude

    private var _declination = 0f
    private var started = false
    private var gotLocation = false
    private var gotAltitude = false

    private val declinationCalculator = DeclinationCalculator()

    init {
        if (gps.hasValidReading && altimeter.hasValidReading) {
            gotLocation = true
            gotAltitude = true
            _declination = calculateDeclination()
        }
    }

    override fun startImpl() {
        started = true
        gps.start(this::onGPSUpdate)
        if (gps == altimeter) {
            gotAltitude = true
        } else {
            altimeter.start(this::onAltimeterUpdate)
        }
    }

    override fun stopImpl() {
        started = false
        gps.stop(this::onGPSUpdate)
        if (gps != altimeter) {
            altimeter.stop(this::onAltimeterUpdate)
        }
    }

    private fun onGPSUpdate(): Boolean {
        gotLocation = true
        if (gotAltitude) {
            _declination = calculateDeclination()
            notifyListeners()
        }
        return started
    }

    private fun onAltimeterUpdate(): Boolean {
        gotAltitude = true
        if (gotLocation) {
            _declination = calculateDeclination()
            notifyListeners()
        }
        return started
    }

    private fun calculateDeclination(): Float {
        return declinationCalculator.calculate(gps.location, altimeter.altitude)
    }

}