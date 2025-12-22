package com.cherrio.jibe.features

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AndroidClipboardProvider(
    private val context: Context
): ClipboardProvider {
    private var lastText: String = ""
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    override val changes: Flow<String> = flow {
        delay(500)
        val text = getClipboardText()
        if (text != null && text != lastText) {
            lastText = text
            emit(text)
        }
    }


    private fun getClipboardText(): String? {
        return clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
    }
    override fun setClipboard(text: String) {
        lastText = text
        clipboard.setPrimaryClip(ClipData.newPlainText("remote", text))
    }

}