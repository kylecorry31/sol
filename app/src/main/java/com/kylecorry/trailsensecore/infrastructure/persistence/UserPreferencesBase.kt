package com.kylecorry.trailsensecore.infrastructure.persistence

import android.content.Context

abstract class UserPreferencesBase(protected val context: Context) {
    protected val cache by lazy { Cache(context) }
}