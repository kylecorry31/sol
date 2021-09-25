package com.kylecorry.sol.science.meteorology.clouds

data class Cloud(
    val name: String,
    val type: CloudType,
    val shape: CloudShape,
    val elevation: CloudHeight
)
