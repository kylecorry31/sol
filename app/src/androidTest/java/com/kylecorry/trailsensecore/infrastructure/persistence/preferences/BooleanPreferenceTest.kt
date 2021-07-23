package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BooleanPreferenceTest {

    private lateinit var cache: Cache
    private val prefName = "boolean_preference_test"

    @Before
    fun setup() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        cache = Cache(ctx)
        cache.remove(prefName)
    }

    @After
    fun teardown() {
        cache.remove(prefName)
    }

    @Test
    fun canGetDefaultValue() {
        val falsePref by BooleanPreference(cache, prefName, false)
        val truePref by BooleanPreference(cache, prefName, true)

        assertFalse(falsePref)
        assertTrue(truePref)
    }

    @Test
    fun canGetValue() {
        cache.putBoolean(prefName, true)
        val pref by BooleanPreference(cache, prefName, false)
        assertTrue(pref)

        cache.putBoolean(prefName, false)
        assertFalse(pref)
    }

    @Test
    fun canSetValue() {
        assertEquals(null, cache.getBoolean(prefName))

        var pref by BooleanPreference(cache, prefName, false)
        pref = true

        assertTrue(pref)
        assertEquals(true, cache.getBoolean(prefName))

        pref = false
        assertFalse(pref)
        assertEquals(false, cache.getBoolean(prefName))
    }

}