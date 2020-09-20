package com.kylecorry.trailsensecore.infrastructure.persistence

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class CacheTest {

    private lateinit var cache: Cache

    @Before
    fun setup(){
        val ctx = InstrumentationRegistry.getInstrumentation().context
        cache = Cache(ctx)
    }

    @Test
    fun cachesDouble(){
        val value = 3.141592654
        cache.putDouble("test_double", value)

        assertEquals(cache.getDouble("test_double"), value)
        assertTrue(cache.contains("test_double"))

        cache.remove("test_double")

        assertNull(cache.getDouble("test_double"))
        assertFalse(cache.contains("test_double"))
    }

    @Test
    fun cachesInt(){
        val value = 1
        val key = "test_int"
        cache.putInt(key, value)

        assertEquals(cache.getInt(key), value)
        assertTrue(cache.contains(key))

        cache.remove(key)

        assertNull(cache.getInt(key))
        assertFalse(cache.contains(key))
    }

    @Test
    fun cachesString(){
        val value = "test"
        val key = "test_string"
        cache.putString(key, value)

        assertEquals(cache.getString(key), value)
        assertTrue(cache.contains(key))

        cache.remove(key)

        assertNull(cache.getString(key))
        assertFalse(cache.contains(key))
    }

    @Test
    fun cachesFloat(){
        val value = 1.2f
        val key = "test_float"
        cache.putFloat(key, value)

        assertEquals(cache.getFloat(key), value)
        assertTrue(cache.contains(key))

        cache.remove(key)

        assertNull(cache.getFloat(key))
        assertFalse(cache.contains(key))
    }

    @Test
    fun cachesBoolean(){
        val value = true
        val key = "test_bool"
        cache.putBoolean(key, value)

        assertEquals(cache.getBoolean(key), value)
        assertTrue(cache.contains(key))

        cache.remove(key)

        assertNull(cache.getBoolean(key))
        assertFalse(cache.contains(key))
    }

    @Test
    fun cachesLong(){
        val value = 2L
        val key = "test_long"
        cache.putLong(key, value)

        assertEquals(cache.getLong(key), value)
        assertTrue(cache.contains(key))

        cache.remove(key)

        assertNull(cache.getLong(key))
        assertFalse(cache.contains(key))
    }

    @Test
    fun updatesCacheValue(){
        val key = "test_int"
        cache.putInt(key, 1)

        assertEquals(cache.getInt(key), 1)

        cache.putInt(key, 2)

        assertEquals(cache.getInt(key), 2)

        cache.remove(key)
    }

}