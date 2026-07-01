/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import org.lineageos.nothing.dirac.pref.DiracPrefs
import org.lineageos.nothing.dirac.util.DiracUtils
import androidx.core.content.edit

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NothingDirac"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Received boot completed intent")

        val sharedPrefs = DiracPrefs.get(context)

        if (!sharedPrefs.contains("dirac_enable")) {
            sharedPrefs.edit {
                putBoolean("dirac_enable", false)
                    .putString("dirac_scenario_pref", "MUSIC")
                    .putString("dirac_preset_pref", "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0")
                    .putInt("dirac_volume_pref", 0)
            }
        }

        DiracUtils.initialize(context)
    }
}