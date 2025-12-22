package com.cherrio.jibe

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.SwingUtilities

// Mostly copied+pasted from stdlib Tray.desktop.kt,

@Composable
fun JibeTray(
    icon: Painter,
    state: TrayState = rememberTrayState(),
    tooltip: String? = null,
    onClick: (position: DpOffset) -> Unit,
    onMenuClick: (position: DpOffset) -> Unit,
) {
    if (!isTraySupported) {
        DisposableEffect(Unit) {
            // We should notify developer, but shouldn't throw an exception.
            // If we would throw an exception, some application wouldn't work on some platforms at
            // all, if developer doesn't check that application crashes.
            //
            // We can do this because we don't return anything in Tray function, and following
            // code doesn't depend on something that is created/calculated in this function.
            System.err.println(
                "Tray is not supported on the current platform. " +
                        "Use the global property `isTraySupported` to check."
            )
            onDispose {}
        }
        return
    }

    val awtIcon = remember(icon) {
        // We shouldn't use LocalDensity here because Tray's density doesn't equal it. It
        // equals to the density of the screen on which it shows. Currently Swing doesn't
        // provide us such information, it only requests an image with the desired width/height
        // (see MultiResolutionImage.getResolutionVariant). Resources like svg/xml should look okay
        // because they don't use absolute '.dp' values to draw, they use values which are
        // relative to their viewport.
        icon.toAwtImage(GlobalDensity, LayoutDirection.Ltr, Size(16f, 16f))
    }

    val tray = remember {
        TrayIcon(awtIcon).apply {
            isImageAutoSize = true

            addMouseListener(object : MouseListener {
                override fun mouseClicked(e: MouseEvent?) {}

                override fun mousePressed(e: MouseEvent?) {
                    if (e == null) return
                    val location = e.locationOnScreen.roundToPx(Density(1f))
                    if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger) {
                        onMenuClick(location)
                    } else {
                        onClick(location)
                    }
                }
                override fun mouseReleased(e: MouseEvent?) {}
                override fun mouseEntered(e: MouseEvent?) {}
                override fun mouseExited(e: MouseEvent?) {}
            })
        }
    }

    SideEffect {
        if (tray.image != awtIcon) tray.image = awtIcon
        if (tray.toolTip != tooltip) tray.toolTip = tooltip
    }

    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        state.notificationFlow
            .onEach(tray::displayMessage)
            .launchIn(coroutineScope)

        SystemTray.getSystemTray().add(tray)
        onDispose {
            SystemTray.getSystemTray().remove(tray)
        }
    }
}

private fun Point.roundToPx(density: Density): DpOffset {
    return with(density) {
        DpOffset(x.toDp(), y.toDp())
    }
}

private fun TrayIcon.displayMessage(notification: Notification) {
    val messageType = when (notification.type) {
        Notification.Type.None -> TrayIcon.MessageType.NONE
        Notification.Type.Info -> TrayIcon.MessageType.INFO
        Notification.Type.Warning -> TrayIcon.MessageType.WARNING
        Notification.Type.Error -> TrayIcon.MessageType.ERROR
    }

    displayMessage(notification.title, notification.message, messageType)
}

internal val GlobalDensity get() = GraphicsEnvironment.getLocalGraphicsEnvironment()
    .defaultScreenDevice
    .defaultConfiguration
    .density

private val GraphicsConfiguration.density: Density
    get() = Density(
        defaultTransform.scaleX.toFloat(),
        fontScale = 1f
    )