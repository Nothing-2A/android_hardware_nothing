/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac

import android.app.Application
import org.lineageos.nothing.dirac.util.DiracUtils

class DiracApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DiracUtils.initialize(this)
    }
}
