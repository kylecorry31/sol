package com.kylecorry.trailsensecore.domain.navigation

import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.units.Coordinate

data class Beacon(
    override val id: Long,
    override val name: String,
    val coordinate: Coordinate,
    val visible: Boolean = true,
    val comment: String? = null,
    val beaconGroupId: Long? = null,
    val elevation: Float? = null,
    val temporary: Boolean = false,
    val owner: BeaconOwner = BeaconOwner.User,
    @ColorInt val color: Int
) : IBeacon