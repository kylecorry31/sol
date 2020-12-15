package com.kylecorry.trailsensecore.infrastructure.system

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


object UiUtils {

    fun alert(
        context: Context,
        title: String,
        content: CharSequence,
        onClose: (() -> Unit)? = null
    ): AlertDialog {
        return alert(context, title, content, R.string.ok, onClose)
    }

    fun alert(
        context: Context,
        title: String,
        content: CharSequence,
        @StringRes okString: Int,
        onClose: (() -> Unit)? = null
    ): AlertDialog {
        return alert(context, title, content, context.getString(okString), onClose)
    }

    fun alert(
        context: Context,
        title: String,
        content: CharSequence,
        okString: String,
        onClose: (() -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setMessage(content)
            setTitle(title)
            setPositiveButton(
                okString
            ) { dialog, _ ->
                onClose?.invoke()
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun alertWithCancel(
        context: Context,
        title: String,
        content: CharSequence,
        @StringRes buttonOk: Int,
        @StringRes buttonCancel: Int,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        return alertWithCancel(
            context,
            title,
            content,
            context.getString(buttonOk),
            context.getString(buttonCancel),
            onClose
        )
    }

    fun alertWithCancel(
        context: Context,
        title: String,
        content: CharSequence,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        return alertWithCancel(
            context,
            title,
            content,
            R.string.ok,
            R.string.cancel,
            onClose
        )
    }

    fun alertWithCancel(
        context: Context,
        title: String,
        content: CharSequence,
        buttonOk: String,
        buttonCancel: String,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setMessage(content)
            setTitle(title)
            setPositiveButton(
                buttonOk
            ) { dialog, _ ->
                onClose?.invoke(false)
                dialog.dismiss()
            }
            setNegativeButton(
                buttonCancel
            ) { dialog, _ ->
                onClose?.invoke(true)
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun alertViewWithCancel(
        context: Context,
        title: String,
        view: View,
        buttonOk: String,
        buttonCancel: String,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setView(view)
            setTitle(title)
            setPositiveButton(
                buttonOk
            ) { dialog, _ ->
                onClose?.invoke(false)
                dialog.dismiss()
            }
            setNegativeButton(
                buttonCancel
            ) { dialog, _ ->
                onClose?.invoke(true)
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun longToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun shortToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    @ColorInt
    fun androidTextColorPrimary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.textColorPrimary)
    }

    @ColorInt
    fun androidBackgroundColorPrimary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.colorBackground)
    }

    @ColorInt
    fun androidBackgroundColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.colorBackgroundFloating)
    }

    @ColorInt
    fun androidTextColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.textColorSecondary)
    }

    @ColorInt
    fun color(context: Context, @ColorRes colorId: Int): Int {
        return ResourcesCompat.getColor(context.resources, colorId, null)
    }

    fun drawable(context: Context, @DrawableRes drawableId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, drawableId, null)
    }

    fun setButtonState(
        button: ImageButton,
        isOn: Boolean,
        @ColorInt primaryColor: Int,
        @ColorInt secondaryColor: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (isOn){
                button.background.setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
                button.setColorFilter(secondaryColor)
            } else {
                button.background.setColorFilter(androidBackgroundColorSecondary(button.context), PorterDuff.Mode.SRC_ATOP)
                button.setColorFilter(androidTextColorSecondary(button.context))
            }
            return
        }

        if (isOn) {
            button.imageTintList = ColorStateList.valueOf(secondaryColor)
            button.backgroundTintList = ColorStateList.valueOf(primaryColor)
        } else {
            button.imageTintList =
                ColorStateList.valueOf(androidTextColorSecondary(button.context))
            button.backgroundTintList =
                ColorStateList.valueOf(androidBackgroundColorSecondary(button.context))
        }
    }

    @ColorInt
    fun getAndroidColorAttr(context: Context, @AttrRes attrRes: Int): Int {
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        val colorRes = if (typedValue.resourceId != 0) typedValue.resourceId else typedValue.data
        return ContextCompat.getColor(context, colorRes)
    }

}