/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom

import java.util.*

/**
 * @author Tom Gaskins
 * @version $Id$
 */
class Intersection {
    var intersectionPoint: Vec4
    var intersectionPosition: Position? = null
    var isTangent: Boolean = false
    var `object`: Any? = null

    /**
     * Constructs an Intersection from an intersection point and tangency indicator.
     *
     * @param intersectionPoint the intersection point.
     * @param isTangent         true if the intersection is tangent to the object intersected, otherwise false.
     *
     * @throws IllegalArgumentException if intersectionPoint is null
     */
    constructor(intersectionPoint: Vec4, isTangent: Boolean) {
        this.intersectionPoint = intersectionPoint
        this.isTangent = isTangent
    }

    constructor(intersectionPoint: Vec4, intersectionPosition: Position?, isTangent: Boolean, `object`: Any?) {
        this.intersectionPoint = intersectionPoint
        this.intersectionPosition = intersectionPosition
        this.isTangent = isTangent
        this.`object` = `object`
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Intersection

        if (isTangent != that.isTangent) return false
        if (intersectionPoint != that.intersectionPoint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = intersectionPoint.hashCode()
        result = 29 * result + if (isTangent) 1 else 0
        return result
    }

    override fun toString(): String {
        val pt = "Intersection Point: $intersectionPoint"
        val tang = if (isTangent) " is a tangent." else " not a tangent"
        return pt + tang
    }

    companion object {
        /**
         * Merges two lists of intersections into a single list sorted by intersection distance from a specified reference
         * point.
         *
         * @param refPoint the reference point.
         * @param listA    the first list of intersections.
         * @param listB    the second list of intersections.
         *
         * @return the merged list of intersections, sorted by increasing distance from the reference point.
         */
        @JvmStatic
        fun sort(refPoint: Vec4, listA: List<Intersection>?, listB: List<Intersection>?): Queue<Intersection> {
            val sorted = PriorityQueue<Intersection>(10) { losiA, losiB ->
                val dA = refPoint.distanceTo3(losiA.intersectionPoint)
                val dB = refPoint.distanceTo3(losiB.intersectionPoint)
                when {
                    dA < dB -> -1
                    dA == dB -> 0
                    else -> 1
                }
            }

            listA?.forEach { sorted.add(it) }
            listB?.forEach { sorted.add(it) }

            return sorted
        }
    }
}
