package com.kylecorry.sol.science.optics

import com.kylecorry.sol.math.Vector3Precise
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PinholeCameraTest {

    @Test
    fun roundTrip() {
        val camera = PinholeCamera(3000, 4000, 3100.0, 0.04)
        val ray = Vector3Precise(0.3, -0.2, 1.0).normalize()
        val (x, y) = camera.toPixel(ray)!!
        val back = camera.toRay(x, y)
        assertEquals(ray.x, back.x, 1e-9)
        assertEquals(ray.y, back.y, 1e-9)
        assertEquals(ray.z, back.z, 1e-9)
    }

    @Test
    fun centerAndAxes() {
        val camera = PinholeCamera.fromVerticalFieldOfView(3000, 4000, 66.0)
        val center = camera.toRay(1500.0, 2000.0)
        assertEquals(1.0, center.z, 1e-12)
        val right = camera.toRay(2000.0, 2000.0)
        val below = camera.toRay(1500.0, 2500.0)
        assertEquals(true, right.x > 0)
        assertEquals(true, below.y > 0)
        assertNull(camera.toPixel(Vector3Precise(0.0, 0.0, -1.0)))
    }
}
