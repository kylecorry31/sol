package com.kylecorry.sol.science.meteorology.clouds

enum class CloudCover(val id: Long) {
    NoClouds(1),
    Few(2),
    Isolated(3),
    Scattered(4),
    Broken(5),
    Overcast(6)
}