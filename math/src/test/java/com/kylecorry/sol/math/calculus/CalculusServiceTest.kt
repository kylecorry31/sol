package com.kylecorry.sol.math.calculus

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.SolMath.cube
import com.kylecorry.sol.math.SolMath.square
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class CalculusServiceTest {

    private val service = CalculusService()

    @Test
    fun derivative1Var() {
        val fn1 = { x: Double -> x * x }
        val gradFn1 = { x: Double -> 2 * x }

        val fn2 = { x: Double -> cube(x) }
        val gradFn2 = { x: Double -> 3 * square(x) }

        assertEquals(
            gradFn1(1.0),
            service.derivative(1.0, step = 0.000001, fn = fn1),
            0.0001
        )
        assertEquals(
            gradFn2(4.0),
            service.derivative(4.0, step = 0.000001, fn = fn2),
            0.0001
        )
    }

    @Test
    fun derivative2Var() {
        val fn1 = { x: Double, y: Double -> x * x + y * y }
        val gradFn1 = { x: Double, y: Double -> 2 * x to 2 * y }

        val fn2 = { x: Double, y: Double -> cube(x) }
        val gradFn2 = { x: Double, y: Double -> 3 * square(x) to 0.0 }

        assertEquals(
            gradFn1(1.0, 3.0).first,
            service.derivative(1.0, 3.0, step = 0.000001, fn = fn1).first,
            0.0001
        )
        assertEquals(
            gradFn1(1.0, 3.0).second,
            service.derivative(1.0, 3.0, step = 0.000001, fn = fn1).second,
            0.0001
        )

        assertEquals(
            gradFn2(4.0, 3.0).first,
            service.derivative(4.0, 3.0, step = 0.000001, fn = fn2).first,
            0.0001
        )
        assertEquals(
            gradFn2(4.0, 3.0).second,
            service.derivative(4.0, 3.0, step = 0.000001, fn = fn2).second,
            0.0001
        )
    }

    @Test
    fun integral1Var(){
        val fn1 = { x: Double -> square(x) }
        val integralFn1 = { x: Double -> cube(x) / 3 }

        assertEquals(
            integralFn1(5.0) - integralFn1(1.0),
            service.integral(1.0, 5.0, step = 0.000001, fn = fn1),
            0.0001
        )

    }
}