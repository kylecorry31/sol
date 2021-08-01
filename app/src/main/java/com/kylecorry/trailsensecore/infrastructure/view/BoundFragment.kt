package com.kylecorry.trailsensecore.infrastructure.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

abstract class BoundFragment<T : ViewBinding> : Fragment() {

    abstract fun generateBinding(layoutInflater: LayoutInflater, container: ViewGroup?): T

    private val permissionActions = mutableMapOf<Int, () -> Unit>()

    protected val binding: T
        get() = _binding!!

    protected val isBound: Boolean
        get() = context != null && _binding != null

    private var _binding: T? = null

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