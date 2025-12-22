package com.cherrio.jibe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.cherrio.jibe.di.Di
import com.cherrio.jibe.features.DesktopClipboardProvider
import jibe.composeapp.generated.resources.Res
import jibe.composeapp.generated.resources.ic_icon
import org.jetbrains.compose.resources.painterResource
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent

fun main() {
    if (System.getProperty("os.name").startsWith("Mac")) {
        System.setProperty("apple.awt.UIElement", "true")
    }
    Di.injectClipboardProvider(DesktopClipboardProvider())
    application {
        var isWindowVisible by remember { mutableStateOf(true) }
        val initialPosition = appPosition()
        val offset by remember(initialPosition) { mutableStateOf(initialPosition) }
        val windowState = rememberWindowState(
            size = DpSize(350.dp, 700.dp),
            position = WindowPosition.Absolute(offset.x, offset.y)
        )
        var currentWindow by remember { mutableStateOf<ComposeWindow?>(null) }
        val trayState = rememberTrayState()
        val focusRequester = remember { FocusRequester() }
        val notification = rememberNotification("Notification", "Message from MyApp!")
        val toolkit = Toolkit.getDefaultToolkit()

        DisposableEffect(Unit){
            val awtListener = AWTEventListener {
                if (it.id == FocusEvent.FOCUS_LOST){
                    isWindowVisible = false
                }
            }
            toolkit.addAWTEventListener(awtListener, AWTEvent.FOCUS_EVENT_MASK)
            onDispose {
                toolkit.removeAWTEventListener(awtListener)
            }
        }
        LaunchedEffect(isWindowVisible) {
            currentWindow?.isAlwaysOnTop = isWindowVisible
        }

        JibeTray(
            state = trayState,
            tooltip = "Jibe",
            icon = TrayIcon(),
            onClick = {
                isWindowVisible = !isWindowVisible

            },
            onMenuClick = {  }
        )

        Window(
            onCloseRequest = { isWindowVisible = false },
            visible = isWindowVisible,
            resizable = false,
            state = windowState,
            title = "Jibe",
            undecorated = true,
            transparent = true,
            icon = painterResource(Res.drawable.ic_icon)
        ) {
            currentWindow = window
            CompositionLocalProvider(LocalAppShutDown provides { Di.close(); exitApplication() }) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).focusRequester(focusRequester),
                ) {
                    App()
                }
            }
        }

    }
}

@Composable
private fun appPosition(): DpOffset {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    return with(LocalDensity.current) {
        DpOffset(screenSize.width.toDp(), 60.toDp())
    }
}
@Composable
private fun TrayIcon(): Painter {
    val painter = rememberVectorPainter(Icons.Default.Cached)
    return object: Painter() {
        override val intrinsicSize: Size
            get() = Size(256f, 256f)

        override fun DrawScope.onDraw() {
            with(painter) {
                draw(size, colorFilter = ColorFilter.tint(Color.White))
            }
        }
    }
}