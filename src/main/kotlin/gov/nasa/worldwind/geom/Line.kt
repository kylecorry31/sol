/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom

/**
 * @author Tom Gaskins
 * @version $Id$
 */
class Line(val origin: Vec4, val direction: Vec4) {

    init {
        require(direction.getLength3() > 0) { "Direction Is Zero Vector" }
    }

    fun getPointAt(t: Double): Vec4 = Vec4.fromLine3(origin, t, direction)

    fun selfDot(): Double = origin.dot3(direction)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val line = other as Line

        if (direction != line.direction) return false
        if (origin != line.origin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 29 * result + direction.hashCode()
        return result
    }

    override fun toString(): String = "Origin: $origin, Direction: $direction"

    fun nearestPointTo(p: Vec4): Vec4 {
        val w = p.subtract3(origin)
        val c1 = w.dot3(direction)
        val c2 = direction.dot3(direction)
        return origin.add3(direction.multiply3(c1 / c2))
    }

    /**
     * Calculate the shortest distance between this line and a specified Vec4. This method returns a
     * positive distance.
     *
     * @param p the Vec4 whose distance from this Line will be calculated
     *
     * @return the distance between this Line and the specified Vec4
     *
     * @throws IllegalArgumentException if p is null
     */
    fun distanceTo(p: Vec4): Double = p.distanceTo3(nearestPointTo(p))

    /**
     * Determine if a point is behind the Line's origin.
     *
     * @param point The point to test.
     *
     * @return true if point is behind this Line's origin, false otherwise.
     */
    fun isPointBehindLineOrigin(point: Vec4): Boolean {
        val dot = point.subtract3(origin).dot3(direction)
        return dot < 0.0
    }

    fun nearestIntersectionPoint(intersections: Array<Intersection>): Vec4? {
        var intersectionPoint: Vec4? = null

        // Find the nearest intersection that's in front of the ray origin.
        var nearestDistance = Double.MAX_VALUE
        for (intersection in intersections) {
            // Ignore any intersections behind the line origin.
            if (!isPointBehindLineOrigin(intersection.intersectionPoint)) {
                val d = intersection.intersectionPoint.distanceTo3(origin)
                if (d < nearestDistance) {
                    intersectionPoint = intersection.intersectionPoint
                    nearestDistance = d
                }
            }
        }

        return intersectionPoint
    }

    companion object {
        /**
         * Create the line containing a line segment between two points.
         *
         * @param pa the first point of the line segment.
         * @param pb the second point of the line segment.
         *
         * @return The line containing the two points.
         *
         * @throws IllegalArgumentException if either point is null or they are coincident.
         */
        @JvmStatic
        fun fromSegment(pa: Vec4, pb: Vec4): Line {
            return Line(pa, Vec4(pb.x - pa.x, pb.y - pa.y, pb.z - pa.z, 0.0))
        }

        /**
         * Finds the closest point to a third point of a segment defined by two points.
         *
         * @param p0 The first endpoint of the segment.
         * @param p1 The second endpoint of the segment.
         * @param p  The point outside the segment whose closest point on the segment is desired.
         *
         * @return The closest point on (p0, p1) to p. Note that this will be p0 or p1 themselves whenever the closest
         *         point on the line defined by p0 and p1 is outside the segment (i.e., the results are bounded by
         *         the segment endpoints).
         */
        @JvmStatic
        fun nearestPointOnSegment(p0: Vec4, p1: Vec4, p: Vec4): Vec4 {
            val v = p1.subtract3(p0)
            val w = p.subtract3(p0)

            val c1 = w.dot3(v)
            val c2 = v.dot3(v)

            if (c1 <= 0)
                return p0
            if (c2 <= c1)
                return p1

            return p0.add3(v.multiply3(c1 / c2))
        }

        @JvmStatic
        fun distanceToSegment(p0: Vec4, p1: Vec4, p: Vec4): Double {
            val pb = nearestPointOnSegment(p0, p1, p)
            return p.distanceTo3(pb)
        }

        /**
         * Clip a line segment to a frustum, returning the end points of the portion of the segment that is within the
         * frustum.
         *
         * @param pa      the first point of the segment.
         * @param pb      the second point of the segment.
         * @param frustum the frustum.
         *
         * @return The two points at which the segment intersects the frustum, or null if the segment does not intersect and
         *         the frustum does not fully contain it. If the segment is coincident with a plane of the frustum, the
         *         returned segment is the portion of the original segment on that plane, clipped to the other frustum
         *         planes.
         */
        @JvmStatic
        @JvmOverloads
        fun clipToFrustum(pa: Vec4, pb: Vec4, frustum: Frustum, maxRecursionCount: Int = 1): Array<Vec4>? {
            // First do a trivial accept test.
            if (frustum.contains(pa) && frustum.contains(pb))
                return arrayOf(pa, pb)

            var segment = arrayOf(pa, pb)

            for (p in frustum.allPlanes) {
                // See if both points are behind the plane and therefore not in the frustum.
                if (p.onSameSide(segment[0], segment[1]) < 0)
                    return null

                // Clip the segment to the plane if they intersect.
                val ipts = p.clip(segment[0], segment[1])
                if (ipts != null) {
                    segment = ipts
                }
            }

            // If one of the initial points was in the frustum then the segment must have been clipped.
            if (frustum.contains(pa) || frustum.contains(pb))
                return segment

            // The segment was clipped by an infinite frustum plane but may still lie outside the frustum.
            // So recurse using the clipped segment.
            if (maxRecursionCount > 0)
                return clipToFrustum(segment[0], segment[1], frustum, maxRecursionCount - 1)
            else
                return segment
        }
    }
}
