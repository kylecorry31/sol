package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.navigation.GravityZenithLocator
import com.kylecorry.sol.science.astronomy.navigation.SkyImage
import com.kylecorry.sol.science.astronomy.navigation.StarIdentification
import com.kylecorry.sol.science.astronomy.navigation.StarIdentifier
import com.kylecorry.sol.science.astronomy.navigation.StarIdentifierSettings
import com.kylecorry.sol.science.geophysics.Geophysics
import com.kylecorry.sol.science.geophysics.VerticalDeflectionModel
import com.kylecorry.sol.units.Coordinate

internal object CelestialNavigationFacade {

    fun identifyStars(
        image: SkyImage,
        approximateLocation: Coordinate,
        settings: StarIdentifierSettings
    ): StarIdentification? {
        return StarIdentifier(settings).identify(image, approximateLocation)
    }

    fun getLocationFromStars(
        image: SkyImage,
        approximateLocation: Coordinate,
        settings: StarIdentifierSettings,
        deflection: VerticalDeflectionModel?
    ): Coordinate? {
        requireNotNull(image.up) { "SkyImage.up is required to get the location" }
        val identifier = StarIdentifier(settings)
        val locator = GravityZenithLocator(settings.withRefraction)

        fun locate(guess: Coordinate): Coordinate? {
            return identifier.identify(image, guess)?.let { locator.getLocation(image, it, guess) }
        }

        var astronomic = locate(approximateLocation)
        // Refraction near the horizon depends on the location, so refit the lens at the new estimate
        repeat(LOCATION_REFINEMENTS) {
            astronomic = astronomic?.let { current -> locate(current) ?: current }
        }
        val result = astronomic
        val correction = result?.let { deflection?.getVerticalDeflection(it) }
        return if (result != null && correction != null) Geophysics.getGeodeticLocation(result, correction) else result
    }

    fun getOrientationFromStars(
        image: SkyImage,
        location: Coordinate,
        settings: StarIdentifierSettings
    ): StarIdentification? {
        val identifier = StarIdentifier(settings)
        val identification = identifier.identify(image, location) ?: return null
        return identification.copy(rotation = identifier.fitRotation(image, identification, location))
    }

    private const val LOCATION_REFINEMENTS = 2
}
