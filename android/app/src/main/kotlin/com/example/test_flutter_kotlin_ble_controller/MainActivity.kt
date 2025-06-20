// MainActivity.kt
package com.example.test_flutter_kotlin_ble_controller

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.test_flutter_kotlin_ble_controller.ui.MainScreen
import java.util.*

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        manager.adapter
    }

    private lateinit var results: MutableList<ScanResult>
    private val gattDeviceNames = mutableStateMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.values.all { granted -> granted }) {
                startContent()
            }
        }

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startContent()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startContent() {
        setContent {
            results = remember { mutableStateListOf<ScanResult>() }
            LaunchedEffect(Unit) {
                startScan(results)
            }
            MainScreen(results, gattDeviceNames)
        }
    }

    private fun startScan(results: MutableList<ScanResult>) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        scanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (results.none { it.device.address == result.device.address }) {
                    results.add(result)
                    connectForName(result.device)
                }
            }
        })
    }

    private fun connectForName(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        )
            return

        device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "✅ Connected to ${device.address}")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val service =
                    gatt.getService(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"))
                val characteristic =
                    service?.getCharacteristic(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"))

                if (characteristic != null) {
                    gatt.readCharacteristic(characteristic)
                } else {
                    Log.w("BLE", "❌ Device name characteristic not found")
                    gatt.disconnect()
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val deviceName = characteristic.getStringValue(0)
                    Log.d("BLE", "📛 GATT device name: $deviceName")
                    gattDeviceNames[gatt.device.address] = deviceName
                }
                gatt.disconnect()
            }
        })
    }
}
