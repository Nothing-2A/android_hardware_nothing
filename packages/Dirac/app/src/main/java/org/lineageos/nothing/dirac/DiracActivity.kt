/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.lineageos.nothing.dirac.util.DiracUtils

class DiracSettingsActivity : ComponentActivity() {

    private val viewModel: DiracViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draws the UI behind the transparent system navigation and status bars
        enableEdgeToEdge()
        DiracUtils.initialize(this)

        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            val colorScheme =
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

            MaterialTheme(colorScheme = colorScheme) {
                // Manages the scroll state to collapse the MediumTopAppBar
                val scrollBehavior =
                    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.dirac_title),
                                )
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { paddingValues ->
                    DiracSettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues) // Prevents double-padding from edge-to-edge
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DiracUtils.release()
    }
}

@Composable
fun DiracSettingsScreen(viewModel: DiracViewModel, modifier: Modifier = Modifier) {
    val isEnabled by viewModel.isEnabled.collectAsState()
    val currentScenario by viewModel.scenario.collectAsState()
    val currentPreset by viewModel.preset.collectAsState()
    val currentVolume by viewModel.volume.collectAsState()

    val scrollState = rememberScrollState()

    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.extraLarge, // 28dp rounding
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setEnabled(!isEnabled) }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.dirac_enable),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.setEnabled(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEnabled) {
            val outerCorner = 28.dp
            val innerCorner = 4.dp
            val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            val topShape = RoundedCornerShape(
                topStart = outerCorner,
                topEnd = outerCorner,
                bottomStart = innerCorner,
                bottomEnd = innerCorner
            )
            val middleShape = RoundedCornerShape(innerCorner)
            val bottomShape = RoundedCornerShape(
                topStart = innerCorner,
                topEnd = innerCorner,
                bottomStart = outerCorner,
                bottomEnd = outerCorner
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(
                    color = cardColor,
                    shape = topShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListPreference(
                        title = stringResource(id = R.string.dirac_audio_title),
                        entries = stringArrayResource(id = R.array.dirac_scenario_pref_entries).toList(),
                        entryValues = stringArrayResource(id = R.array.dirac_scenario_pref_values).toList(),
                        currentValue = currentScenario,
                        onValueChanged = { viewModel.setScenario(it) }
                    )
                }

                Surface(
                    color = cardColor,
                    shape = middleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListPreference(
                        title = stringResource(id = R.string.dirac_preset_title),
                        entries = stringArrayResource(id = R.array.dirac_preset_pref_entries).toList(),
                        entryValues = stringArrayResource(id = R.array.dirac_preset_pref_values).toList(),
                        currentValue = currentPreset,
                        onValueChanged = { viewModel.setPreset(it) }
                    )
                }

                Surface(
                    color = cardColor,
                    shape = bottomShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Text(
                            text = stringResource(id = R.string.dirac_preamp_volume_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Slider(
                                value = currentVolume.toFloat(),
                                onValueChange = { newValue ->
                                    val newInt = newValue.toInt()
                                    if (newInt != currentVolume) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        viewModel.setVolume(newInt)
                                    }
                                },
                                valueRange = 0f..16f,
                                steps = 15,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = currentVolume.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // Slightly inset from the edges
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.dirac_info_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ListPreference(
    title: String,
    entries: List<String>,
    entryValues: List<String>,
    currentValue: String,
    onValueChanged: (String) -> Unit
) {
    var showDialog = remember { mutableStateOf(false) }
    val currentIndex = entryValues.indexOf(currentValue).takeIf { it >= 0 } ?: 0

    // The actual preference row that sits inside the card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog.value = true }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = entries[currentIndex],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Makes the list scrollable if presets overflow the screen
                        .verticalScroll(rememberScrollState())
                ) {
                    entries.forEachIndexed { index, entry ->
                        val isSelected = entryValues[index] == currentValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChanged(entryValues[index])
                                    showDialog.value = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onValueChanged(entryValues[index])
                                    showDialog.value = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}