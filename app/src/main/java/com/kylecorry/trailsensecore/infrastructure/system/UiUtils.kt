package com.kylecorry.trailsensecore.infrastructure.system

import android.content.res.ColorStateList
import android.widget.ImageButton
import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.system.Resources


object UiUtils {

    fun setButtonState(
        button: ImageButton,
        isOn: Boolean,
        @ColorInt primaryColor: Int,
        @ColorInt secondaryColor: Int
    ) {
        if (isOn) {
            button.imageTintList = ColorStateList.valueOf(secondaryColor)
            button.backgroundTintList = ColorStateList.valueOf(primaryColor)
        } else {
            button.imageTintList =
                ColorStateList.valueOf(Resources.androidTextColorSecondary(button.context))
            button.backgroundTintList =
                ColorStateList.valueOf(Resources.androidBackgroundColorSecondary(button.context))
        }
    }
}