package com.example.bluetooth_iot

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bluetooth_iot.ui.components.DeviceSelectionDialog
import com.example.bluetooth_iot.ui.components.HeaderSection
import com.example.bluetooth_iot.ui.components.NeomorphicCurtainCard
import com.example.bluetooth_iot.ui.components.NeomorphicIconCard
import com.example.bluetooth_iot.ui.theme.ModernSkeuomorphicTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothHelper: BluetoothHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        bluetoothHelper = BluetoothHelper(this)

        setContent {
            ModernSkeuomorphicTheme {
                HomeIotApp(bluetoothHelper)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothHelper.unregister()
    }
}

@Composable
fun HomeIotApp(bluetoothHelper: BluetoothHelper) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    var pairedDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    val isConnected by bluetoothHelper.isConnected.collectAsState()
    var showDeviceDialog by remember { mutableStateOf(false) }

    val isTablet = configuration.screenWidthDp >= 600

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.all { it }) {
            pairedDevices = bluetoothHelper.getPairedDevices()
            showDeviceDialog = true
        } else {
            Toast.makeText(context, "Permissions required", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFE0E5EC)
    ) { padding ->
        val gridCells = if (isTablet) GridCells.Fixed(2) else GridCells.Fixed(1)
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = if (isTablet) 48.dp else 24.dp)
        ) {
            HeaderSection(isConnected) {
                if (isConnected) {
                    bluetoothHelper.disconnect()
                } else {
                    val missing = permissions.filter {
                        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (missing.isEmpty()) {
                        pairedDevices = bluetoothHelper.getPairedDevices()
                        showDeviceDialog = true
                    } else {
                        launcher.launch(permissions)
                    }
                }
            }

            LazyVerticalGrid(
                columns = gridCells,
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    NeomorphicIconCard("Cameras", Icons.Outlined.Videocam, Color(0xFF1A73E8))
                }
                item {
                    NeomorphicIconCard("Lights", Icons.Outlined.Lightbulb, Color(0xFFF9AB00))
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Smart Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF444444),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NeomorphicCurtainCard(
                        isConnected = isConnected,
                        onOpen = { seconds -> bluetoothHelper.sendCommand("O$seconds") },
                        onClose = { seconds -> bluetoothHelper.sendCommand("C$seconds") }
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { 
                    Spacer(modifier = Modifier.height(100.dp)) 
                }
            }
        }
    }

    if (showDeviceDialog) {
        DeviceSelectionDialog(
            devices = pairedDevices,
            onDeviceSelected = { device ->
                scope.launch {
                    val success = bluetoothHelper.connect(device)
                    if (success) Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show()
                    showDeviceDialog = false
                }
            },
            onDismiss = { showDeviceDialog = false }
        )
    }
}
