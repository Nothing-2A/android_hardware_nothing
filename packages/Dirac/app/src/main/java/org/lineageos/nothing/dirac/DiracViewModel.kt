/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.nothing.dirac.pref.DiracPrefs
import org.lineageos.nothing.dirac.util.DiracUtils
import androidx.core.content.edit

class DiracViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = DiracPrefs.get(application)

    private val _isEnabled = MutableStateFlow(prefs.getBoolean("dirac_enable", false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _scenario =
        MutableStateFlow(prefs.getString("dirac_scenario_pref", "MUSIC") ?: "MUSIC")
    val scenario: StateFlow<String> = _scenario.asStateFlow()

    private val _preset = MutableStateFlow(
        prefs.getString(
            "dirac_preset_pref",
            "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0"
        ) ?: ""
    )
    val preset: StateFlow<String> = _preset.asStateFlow()

    private val _volume = MutableStateFlow(prefs.getInt("dirac_volume_pref", 10))
    val volume: StateFlow<Int> = _volume.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("dirac_enable", enabled) }
        _isEnabled.value = enabled
        DiracUtils.setEnabled(enabled, getApplication())
    }

    fun setScenario(scenario: String) {
        prefs.edit { putString("dirac_scenario_pref", scenario) }
        _scenario.value = scenario
        DiracUtils.setScenario(scenario)
    }

    fun setPreset(preset: String) {
        prefs.edit { putString("dirac_preset_pref", preset) }
        _preset.value = preset
        DiracUtils.setLevel(preset)
    }

    fun setVolume(volume: Int) {
        prefs.edit { putInt("dirac_volume_pref", volume) }
        _volume.value = volume
        DiracUtils.setVolume(volume)
    }
}
