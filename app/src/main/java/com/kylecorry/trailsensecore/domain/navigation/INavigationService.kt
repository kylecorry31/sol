package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.Distance
import java.time.Duration

interface INavigationService {

    fun triangulate(pointA: Coordinate, bearingA: Bearing, pointB: Coordinate, bearingB: Bearing): Coordinate?
    fun deadReckon(lastLocation: Coordinate, distanceTravelled: Float, bearingToLast: Bearing): Coordinate

    fun navigate(
        from: Coordinate,
        to: Coordinate,
        declination: Float,
        useTrueNorth: Boolean
    ): NavigationVector

    fun destination(from: Coordinate, distance: Float, bearing: Bearing): Coordinate

    fun getPaceDistance(paces: Int, paceLength: Distance): Distance

    fun getPaces(steps: Int): Int

    fun getPaceLength(paces: Int, distanceTravelled: Distance): Distance

    fun getPathDistance(points: List<Coordinate>): Distance
}