package com.cherrio.jibe.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cherrio.jibe.design.JibeConnectedGreen
import com.cherrio.jibe.design.JibeOfflineGray
import com.cherrio.jibe.di.Di
import com.cherrio.jibe.features.PluginManager
import com.cherrio.jibe.rememberQrCode
import com.cherrio.jibe.rememberShutDown
import io.github.alexzhirkevich.qrose.options.*
import io.github.alexzhirkevich.qrose.rememberQrCodePainter


@Composable
fun HomeScreen(){
    val viewModel = viewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pluginManager = Di.get<PluginManager>()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> pluginManager.startPlugins()
                Lifecycle.Event.ON_STOP -> pluginManager.stopPlugins()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeContent(state = state)
}

@Composable
private fun HomeContent(
    state: HomeState
) {
    val painter = state.connectionDevice?.let {
        rememberQrCodePainter(it.ip){
            shapes {
                ball = QrBallShape.circle()
                darkPixel = QrPixelShape.roundCorners()
                frame = QrFrameShape.roundCorners(.25f)
            }
            colors {
                dark = QrBrush.solid(Color.Black)
                frame = QrBrush.solid(Color.Black)
            }
        }
    }
    val qrLauncher = rememberQrCode()
    val shutdown = rememberShutDown()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { JibeTopAppBar{ shutdown() } },
        floatingActionButton = { if (state.isMobile){ ConnectDeviceButton{ qrLauncher() } } },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.device?.let { device ->
                DeviceItem(
                    icon = device.getDeviceIcon(),
                    name = device.name,
                    isOnline = state.isConnected
                )
            }
            if (state.device == null && painter != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painter,
                        contentDescription = "QR code",
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JibeTopAppBar(
    onClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Jibe",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun DeviceItem(icon: ImageVector, name: String, isOnline: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusIndicator(isOnline = isOnline)
            }
        }
    }
}

@Composable
private fun StatusIndicator(isOnline: Boolean) {
    val statusText = if (isOnline) "Connected" else "Offline"
    val statusColor = if (isOnline) JibeConnectedGreen else JibeOfflineGray

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Text(
            text = statusText,
            color = statusColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ConnectDeviceButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Filled.Add, "Add new device") },
        text = {
            Text(
                text = "Connect new device",
                style = MaterialTheme.typography.labelLarge
            )
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

//@Preview(showBackground = true, name = "Light Mode")
//@Preview(showBackground = true)
//@Composable
//fun JibeScreenPreview() {
//    JibeTheme {
//        JibeScreen()
//    }
//}