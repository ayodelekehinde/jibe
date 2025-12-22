package com.cherrio.jibe.features

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import kotlin.time.Duration.Companion.seconds

class DesktopClipboardProvider: ClipboardProvider {
    private var lastText: String = ""
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    override val changes: Flow<String> = flow {
        while (currentCoroutineContext().isActive) {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                val text = clipboard.getData(DataFlavor.stringFlavor) as? String
                if (text != null && text != lastText) {
                    lastText = text
                    emit(text)
                }
            }
            delay(1.seconds)
        }
    }

    override fun setClipboard(text: String) {
        lastText = text
        clipboard.setContents(StringSelection(text), null)
    }
}