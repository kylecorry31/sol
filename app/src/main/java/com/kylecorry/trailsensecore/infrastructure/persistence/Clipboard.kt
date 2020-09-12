package com.kylecorry.trailsensecore.infrastructure.persistence

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService

class Clipboard(private val context: Context) {
    private val clipboardManager = context.getSystemService<ClipboardManager>()

    fun copy(text: String, toastMessage: String? = null) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText(text, text))

        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

}