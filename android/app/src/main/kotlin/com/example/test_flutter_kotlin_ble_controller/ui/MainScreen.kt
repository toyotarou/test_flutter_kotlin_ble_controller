package com.example.test_flutter_kotlin_ble_controller.ui

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(results: List<ScanResult>, gattDeviceNames: Map<String, String>) {
    val context = LocalContext.current // ✅ context をここで取得

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE Scan Results") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(results) { result ->
                val device = result.device
                val address = device.address
                val gattName = gattDeviceNames[address] ?: "GATT未取得"
                val scanRecordName = result.scanRecord?.deviceName ?: "null"

                // ✅ context を使って BLUETOOTH_CONNECT チェック
                val deviceName = if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    device.name ?: "null"
                } else {
                    "許可なし"
                }

                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "🔢 Address: $address")
                    Text(text = "📛 GATT Name: $gattName")
                    Text(text = "🧠 ScanRecord.deviceName: $scanRecordName")
                    Text(text = "📱 device.name: $deviceName")
                    Text(text = "📶 RSSI: ${result.rssi}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
