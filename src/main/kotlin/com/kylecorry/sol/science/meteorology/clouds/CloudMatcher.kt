package com.kylecorry.sol.science.meteorology.clouds

internal interface CloudMatcher {
    fun matches(genus: CloudGenus?): Boolean
}

internal class CloudGenusMatcher(private val clouds: List<CloudGenus?>) : CloudMatcher {
    override fun matches(genus: CloudGenus?): Boolean {
        return clouds.contains(genus)
    }
}