package com.example.bluetooth_iot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothHelper(private val context: Context) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d("BluetoothHelper", "Device disconnected: ${device?.address}")
                _isConnected.value = false
                try {
                    socket?.close()
                } catch (e: IOException) {
                    Log.e("BluetoothHelper", "Error closing socket on disconnect", e)
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(bluetoothReceiver, filter)
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return adapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                adapter?.cancelDiscovery()
                socket?.connect()
                _isConnected.value = true
                true
            } catch (e: IOException) {
                Log.e("BluetoothHelper", "Connection failed", e)
                cleanup()
                false
            }
        }
    }

    fun sendCommand(command: String) {
        if (socket?.isConnected == true) {
            try {
                socket?.outputStream?.write(command.toByteArray())
            } catch (e: IOException) {
                Log.e("BluetoothHelper", "Error sending data", e)
                _isConnected.value = false
                cleanup()
            }
        } else if (_isConnected.value) {
            _isConnected.value = false
        }
    }

    fun disconnect() {
        cleanup()
    }

    private fun cleanup() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e("BluetoothHelper", "Error closing socket", e)
        }
        socket = null
        _isConnected.value = false
    }
    
    fun unregister() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Log.e("BluetoothHelper", "Error unregistering receiver", e)
        }
    }
}
