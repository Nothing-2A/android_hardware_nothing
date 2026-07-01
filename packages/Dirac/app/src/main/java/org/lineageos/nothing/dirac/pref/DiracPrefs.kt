/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac.pref

import android.content.Context
import android.content.SharedPreferences

object DiracPrefs {
    private const val PREFS_NAME = "org.lineageos.nothing.dirac_preferences"

    fun get(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}