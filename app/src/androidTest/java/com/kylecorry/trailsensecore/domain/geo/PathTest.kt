package com.kylecorry.trailsensecore.domain.geo

import com.kylecorry.andromeda.core.units.Bearing
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.core.units.DistanceUnits
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant

class PathTest {

    @Test
    fun distanceNoPoints() {
        val points = listOf<PathPoint>()
        val path = Path(1, "", points, 0)
        val distance = path.distance
        assertEquals(0f, distance.distance, 0.00001f)
        assertEquals(DistanceUnits.Meters, distance.units)
    }

    @Test
    fun distanceOnePoint() {
        val points = listOf(
            PathPoint(1, 1, Coordinate(0.0, 0.0))
        )
        val path = Path(1, "", points, 0)
        val distance = path.distance
        assertEquals(0f, distance.distance, 0.00001f)
        assertEquals(DistanceUnits.Meters, distance.units)
    }

    @Test
    fun distanceMultiplePoints() {
        val points = listOf(
            PathPoint(1, 1, Coordinate(0.0, 0.0).plus(10.0, Bearing(0f))),
            PathPoint(2, 1, Coordinate(0.0, 0.0).plus(10.0, Bearing(0f)).plus(100.0, Bearing(90f))),
            PathPoint(
                3,
                1,
                Coordinate(0.0, 0.0).plus(10.0, Bearing(0f)).plus(100.0, Bearing(90f))
                    .plus(200.0, Bearing(0f))
            ),
        )
        val path = Path(1, "", points, 0)
        val distance = path.distance
        assertEquals(300f, distance.distance, 1.5f)
        assertEquals(DistanceUnits.Meters, distance.units)
    }

    @Test
    fun durationNoPoints(){
        val points = listOf<PathPoint>()
        val path = Path(1, "", points, 0)
        val duration = path.duration
        assertNull(duration)
    }

    @Test
    fun durationNoTimes(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = null)
        )
        val path = Path(1, "", points, 0)
        val duration = path.duration
        assertNull(duration)
    }

    @Test
    fun durationOnePoint(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = Instant.ofEpochMilli(100))
        )
        val path = Path(1, "", points, 0)
        val duration = path.duration
        assertEquals(0L, duration!!.seconds)
    }

    @Test
    fun durationMultiplePoints(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = Instant.ofEpochMilli(0)),
            PathPoint(2, 1, Coordinate.zero, time = Instant.ofEpochMilli(1000)),
            PathPoint(3, 1, Coordinate.zero, time = Instant.ofEpochMilli(5000)),
        )
        val path = Path(1, "", points, 0)
        val duration = path.duration
        assertEquals(5L, duration!!.seconds)
    }

    @Test
    fun lastPoint(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = Instant.ofEpochMilli(0)),
            PathPoint(2, 1, Coordinate.zero, time = Instant.ofEpochMilli(1000)),
            PathPoint(3, 1, Coordinate.zero, time = Instant.ofEpochMilli(5000)),
        )
        val path = Path(1, "", points, 0)
        assertEquals(3, path.lastPoint!!.id)
    }

    @Test
    fun lastPointNoPoints(){
        val points = listOf<PathPoint>()
        val path = Path(1, "", points, 0)
        assertNull(path.lastPoint)
    }

    @Test
    fun deleteOnNoPoints(){
        val points = listOf<PathPoint>()
        val path = Path(1, "", points, 0, deleteAfter = Duration.ofSeconds(10))
        assertNull(path.deleteOn)
    }

    @Test
    fun deleteOnNoTimes(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = null),
        )
        val path = Path(1, "", points, 0, deleteAfter = Duration.ofSeconds(10))
        assertNull(path.deleteOn)
    }

    @Test
    fun deleteOn(){
        val points = listOf(
            PathPoint(1, 1, Coordinate.zero, time = Instant.ofEpochMilli(0)),
            PathPoint(2, 1, Coordinate.zero, time = Instant.ofEpochMilli(1000)),
            PathPoint(3, 1, Coordinate.zero, time = Instant.ofEpochMilli(5000)),
        )
        val path = Path(1, "", points, 0, deleteAfter = Duration.ofSeconds(10))
        assertEquals(15000L, path.deleteOn?.toEpochMilli())
    }

}