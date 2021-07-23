package com.kylecorry.trailsensecore.infrastructure.persistence.preferences

import androidx.test.platform.app.InstrumentationRegistry
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StringEnumPreferenceTest {

    private lateinit var cache: Cache
    private val prefName = "string_enum_preference_test"

    private val mapping = mapOf(
        "1" to TestEnum.One,
        "2" to TestEnum.Two,
        "3" to TestEnum.Three
    )

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
        val onePref by StringEnumPreference(cache, prefName, mapping, TestEnum.One)
        val twoPref by StringEnumPreference(cache, prefName, mapping, TestEnum.Two)

        assertEquals(TestEnum.One, onePref)
        assertEquals(TestEnum.Two, twoPref)
    }

    @Test
    fun canGetValue() {
        cache.putString(prefName, "2")
        val pref by StringEnumPreference(cache, prefName, mapping, TestEnum.One)
        assertEquals(TestEnum.Two, pref)

        cache.putString(prefName, "3")
        assertEquals(TestEnum.Three, pref)
    }

    @Test
    fun canGetDefaultValueWhenNoMappingExists() {
        cache.putString(prefName, "4")
        val pref by StringEnumPreference(cache, prefName, mapping, TestEnum.One)

        assertEquals(TestEnum.One, pref)
    }

    @Test
    fun canSetValue() {
        assertEquals(null, cache.getString(prefName))

        var pref by StringEnumPreference(cache, prefName, mapping, TestEnum.One)
        pref = TestEnum.Two

        assertEquals(TestEnum.Two, pref)
        assertEquals("2", cache.getString(prefName))

        pref = TestEnum.Three
        assertEquals(TestEnum.Three, pref)
        assertEquals("3", cache.getString(prefName))
    }

    @Test
    fun doesNotSetValueWhenNoMappingExists() {
        cache.putString(prefName, "2")
        var pref by StringEnumPreference(cache, prefName, mapping, TestEnum.One)
        pref = TestEnum.Four

        assertEquals(TestEnum.Two, pref)
        assertEquals("2", cache.getString(prefName))
    }

    private enum class TestEnum {
        One,
        Two,
        Three,
        Four
    }

}