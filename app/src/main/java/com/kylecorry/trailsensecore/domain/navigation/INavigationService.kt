package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.trailsensecore.domain.Coordinate
import java.time.Duration

interface INavigationService {

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean
    ): NavigationVector

    fun navigate(
        from: Position,
        to: Beacon,
        declination: Float,
        usingTrueNorth: Boolean = true
    ): NavigationVector

    fun eta(from: Position, to: Beacon, nonLinear: Boolean): Duration

}