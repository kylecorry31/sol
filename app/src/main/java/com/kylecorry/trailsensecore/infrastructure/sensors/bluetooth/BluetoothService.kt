package com.kylecorry.trailsensecore.infrastructure.sensors.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils

class BluetoothService(private val context: Context) {

    private val adapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    val isEnabled: Boolean
        @SuppressLint("MissingPermission")
        get(){
            return if (PermissionUtils.hasPermission(context, Manifest.permission.BLUETOOTH)) {
                adapter.isEnabled
            } else {
                false
            }
        }

    val devices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get(){
            return if (PermissionUtils.hasPermission(context, Manifest.permission.BLUETOOTH)) {
                adapter.bondedDevices.toList()
            } else {
                listOf()
            }
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter.getRemoteDevice(address)
        } catch (e: Exception){
            null
        }
    }

}