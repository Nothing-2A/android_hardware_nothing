/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac.util

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lineageos.providers.LineageSettings

class BlackThemeObserver(private val context: Context) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    private val _enabled = MutableStateFlow(readValue())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    init {
        context.contentResolver.registerContentObserver(
            LineageSettings.Secure.getUriFor(LineageSettings.Secure.BERRY_BLACK_THEME),
            false,
            this,
        )
    }

    override fun onChange(selfChange: Boolean) {
        _enabled.value = readValue()
    }

    private fun readValue() = LineageSettings.Secure.getInt(
        context.contentResolver,
        LineageSettings.Secure.BERRY_BLACK_THEME,
        0,
    ) != 0

    fun release() {
        context.contentResolver.unregisterContentObserver(this)
    }
}
