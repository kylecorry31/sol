package com.kylecorry.trailsensecore.domain.navigation

import com.kylecorry.trailsensecore.domain.Coordinate

data class Beacon(val id: Int, val name: String, val coordinate: Coordinate, val visible: Boolean = true, val comment: String? = null, val beaconGroupId: Int? = null, val elevation: Float? = null)