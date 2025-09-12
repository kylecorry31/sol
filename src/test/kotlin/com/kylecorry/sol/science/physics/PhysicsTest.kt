package com.kylecorry.sol.science.physics

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.math.Vector3
import com.kylecorry.sol.units.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class PhysicsTest {

    @ParameterizedTest
    @MethodSource("provideFallHeights")
    fun fallHeight(time: Duration, heightMeters: Float) {
        val actualHeight = Physics.fallHeight(time)
        assertEquals(heightMeters, actualHeight.meters().value, 0.0001f)
    }

    @ParameterizedTest
    @MethodSource("provideIsMetal")
    fun isMetal(field: Vector3, threshold: Float, expected: Boolean) {
        assertEquals(expected, Physics.isMetal(field, threshold))
    }

    @Test
    fun getTrajectory2D() {
        val initialVelocity = Speed.from(2670f, DistanceUnits.Feet, TimeUnits.Seconds).convertTo(
            DistanceUnits.Meters,
            TimeUnits.Seconds
        ).speed

        val sightIn = Distance.from(100f, DistanceUnits.Yards).meters().value

        val scopeHeight = Distance.from(1.5f, DistanceUnits.Inches).meters().value

        val initialVelocityVector = Physics.getVelocityVectorForImpact(
            Vector2(sightIn, 0f),
            initialVelocity,
            Vector2(0f, -scopeHeight),
            timeStep = 0.01f,
            maxTime = 1f,
            minAngle = 0f,
            maxAngle = 1f,
            angleStep = 0.001f
        )

        val launchAngle = initialVelocityVector.angle()

        val trajectory = Physics.getTrajectory2D(
            initialPosition = Vector2(0f, -scopeHeight),
            initialVelocity = initialVelocityVector,
            dragModel = NoDragModel(),
            timeStep = 0.01f,
            maxTime = 1f
        )

        // .308 winchester, BC 0.474
        val expectedTrajectory = listOf(
            Triple(
                0f, // Time
                Vector2(0f, -1.5f), // Position (yards, inches)
                2670f // Velocity (feet/s)
            ),
            Triple(
                0.12f, // Time
                Vector2(100f, 0f), // Position (yards, inches)
                2670f // Velocity (feet/s)
            ),
            Triple(
                0.24f, // Time
                Vector2(200f, -4.1f), // Position (yards, inches)
                2670f // Velocity (feet/s)
            ),
            Triple(
                0.38f, // Time
                Vector2(300f, -14.8f), // Position (yards, inches)
                2670f // Velocity (feet/s)
            )
        )

        for (expected in expectedTrajectory) {
            val closestBefore = trajectory.filter { it.time <= expected.first }
                .maxBy { it.time }
            val closestAfter = trajectory.filter { it.time >= expected.first }
                .minBy { it.time }

            val percent = SolMath.norm(expected.first, closestBefore.time, closestAfter.time)

            val actual = TrajectoryPoint2D(
                expected.first,
                Vector2(
                    SolMath.lerp(percent, closestBefore.position.x, closestAfter.position.x),
                    SolMath.lerp(percent, closestBefore.position.y, closestAfter.position.y)
                ),
                Vector2(
                    SolMath.lerp(percent, closestBefore.velocity.x, closestAfter.velocity.x),
                    SolMath.lerp(percent, closestBefore.velocity.y, closestAfter.velocity.y)
                )
            )


            assertEquals(
                expected.first, actual.time, 0.05f,
                "Time did not match for $expected"
            )

            // These values are so high because drag is not accounted for yet
            assertEquals(
                expected.second.x,
                Distance.meters(actual.position.x).convertTo(DistanceUnits.Yards).value,
                40f,
                "Position X did not match for $expected"
            )
            assertEquals(
                expected.second.y,
                Distance.meters(actual.position.y).convertTo(DistanceUnits.Inches).value,
                2f,
                "Position Y did not match for $expected"
            )
            assertEquals(
                expected.third,
                Speed.from(actual.velocity.x, DistanceUnits.Meters, TimeUnits.Seconds).convertTo(
                    DistanceUnits.Feet,
                    TimeUnits.Seconds
                ).speed,
                0.1f,
                "Velocity did not match for $expected"
            )
        }
    }

    @ParameterizedTest
    @CsvSource("1, 1, 0.5")
    @CsvSource("2, 1, 1")
    @CsvSource("1, 2, 2")
    fun getKineticEnergy(mass: Float, speed: Float, expected: Float) {
        val kilograms = Weight.from(mass, WeightUnits.Kilograms)
        val metersPerSecond = Speed.from(speed, DistanceUnits.Meters, TimeUnits.Seconds)
        val joules = Physics.getKineticEnergy(kilograms, metersPerSecond).convertTo(
            EnergyUnits.Joules
        ).value
        assertEquals(expected, joules, 0.01f)
    }


    companion object {
        @JvmStatic
        fun provideIsMetal(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Vector3(0f, 1f, 0f), 0f, true),
                Arguments.of(Vector3(0f, 1f, 0f), 1f, true),
                Arguments.of(Vector3(0f, 1f, 0f), 2f, false),
            )
        }

        @JvmStatic
        fun provideFallHeights(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Duration.ZERO, 0f),
                Arguments.of(Duration.ofSeconds(1), 4.905f),
                Arguments.of(Duration.ofSeconds(2), 19.62f),
            )
        }
    }

}