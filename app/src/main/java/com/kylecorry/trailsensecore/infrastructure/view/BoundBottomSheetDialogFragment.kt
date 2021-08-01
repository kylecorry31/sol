package com.kylecorry.trailsensecore.infrastructure.view

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

abstract class BoundBottomSheetDialogFragment<T : ViewBinding> : BottomSheetDialogFragment() {

    abstract fun generateBinding(layoutInflater: LayoutInflater, container: ViewGroup?): T

    protected val binding: T
        get() = _binding!!

    protected val isBound: Boolean
        get() = context != null && _binding != null

    private var _binding: T? = null

    private val permissionActions = mutableMapOf<Int, () -> Unit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = generateBinding(inflater, container)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun show(fragment: Fragment, tag: String = javaClass.name) {
        show(fragment.requireActivity(), tag)
    }

    fun show(activity: FragmentActivity, tag: String = javaClass.name) {
        show(activity.supportFragmentManager, tag)
    }

    protected fun requestPermissions(
        requestCode: Int,
        permissions: List<String>,
        action: () -> Unit
    ) {
        if (permissions.all { PermissionUtils.hasPermission(requireContext(), it) }) {
            action.invoke()
            return
        }

        permissionActions[requestCode] = action
        PermissionUtils.requestPermissions(
            this,
            permissions,
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val action = permissionActions[requestCode]
        permissionActions.remove(requestCode)
        action?.invoke()
    }
}