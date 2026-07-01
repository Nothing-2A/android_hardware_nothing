/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac.util

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.SystemProperties
import android.util.Log
import android.view.KeyEvent

import org.lineageos.nothing.dirac.DiracSound
import org.lineageos.nothing.dirac.pref.DiracPrefs

class DiracUtils(private val context: Context) {

    private var mMediaSessionManager: MediaSessionManager? = null
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        private var mDiracSound: DiracSound? = null
        private var mInitialized = false
        private const val TAG = "DiracUtils"

        fun initialize(context: Context) {
            if (!mInitialized) {
                mDiracSound = DiracSound(0, 0)

                val sharedPrefs = DiracPrefs.get(context)

                val isEnabled = sharedPrefs.getBoolean("dirac_enable", false)
                setEnabled(isEnabled)

                val savedScenario = sharedPrefs.getString("dirac_scenario_pref", "MUSIC")
                setScenario(savedScenario)

                val savedPreset = sharedPrefs.getString(
                    "dirac_preset_pref",
                    "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0"
                )
                setLevel(savedPreset)

                mInitialized = true
            }
        }

        fun release() {
            mDiracSound?.release()
            mDiracSound = null
            mInitialized = false
        }

        fun setEnabled(enable: Boolean, context: Context? = null) {
            Log.i(TAG, "setEnabled: $enable")

            if (mDiracSound == null) {
                Log.e(TAG, "setEnabled: mDiracSound is NULL! TERRAIN TERRAIN, PULL UP.")
            }

            mDiracSound?.let {
                try {
                    it.setEnabled(enable)
                    Log.i(TAG, "HAL accepted setEnabled")
                } catch (e: Exception) {
                    Log.e(TAG, "HAL rejected setEnabled for value $enable", e)
                }
            } ?: return

            if (enable) {
                context?.let { DiracUtils(it).refreshPlaybackIfNecessary() }
            }

            val value = if (enable) "1.000000" else "0.000000"
            try {
                SystemProperties.set("persist.sys.dirac.enable", value)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set persist.sys.dirac.enable to $value", e)
            }
            Log.i(TAG, "Set Dirac enable to $value")
        }

        fun setScenario(scenario: String?) {
            try {
                SystemProperties.set("persist.sys.dirac.scenario", scenario)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set persist.sys.dirac.scenario to $scenario", e)
            }
            Log.i(TAG, "Set Dirac scenario to $scenario")
        }

        fun setLevel(preset: String?) {
            Log.i(TAG, "setLevel: $preset")

            val dirac = mDiracSound
            if (dirac == null) {
                Log.e(TAG, "setLevel: mDiracSound is NULL! TERRAIN TERRAIN, PULL UP.")
                return
            }

            val level = preset?.split("\\s*,\\s*".toRegex()) ?: return

            level.forEachIndexed { band, value ->
                try {
                    dirac.setLevel(band, value.toFloat())
                    Log.i(TAG, "HAL accepted write for band $band")
                } catch (e: Exception) {
                    Log.e(TAG, "HAL rejected write for band $band", e)
                }
            }
        }

        fun setVolume(level: Int) {
            Log.i(TAG, "setVolume: $level")

            if (mDiracSound == null) {
                Log.e(TAG, "setVolume: mDiracSound is NULL! TERRAIN TERRAIN, PULL UP.")
            }

            mDiracSound?.let {
                try {
                    it.setVolume(level)
                    Log.i(TAG, "HAL accepted setVolume")
                } catch (e: Exception) {
                    Log.e(TAG, "HAL rejected setVolume $level", e)
                }
            }

            val floatString = "%d.000000".format(level)
            try {
                SystemProperties.set("persist.sys.dirac.volume", floatString)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set persist.sys.dirac.volume to $level", e)
            }
            Log.i(TAG, "Set Dirac volume to $floatString")
        }
    }

    fun refreshPlaybackIfNecessary() {
        if (mMediaSessionManager == null) {
            mMediaSessionManager =
                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        }
        // getActiveSessionsForUser/@hide — getActiveSessions(null) is public and sufficient.
        val sessions: List<MediaController> =
            mMediaSessionManager?.getActiveSessions(null) ?: return
        sessions.firstOrNull { PlaybackState.STATE_PLAYING == getMediaControllerPlaybackState(it) }
            ?.let { triggerPlayPause(it) }
    }

    fun triggerPlayPause(controller: MediaController) {
        val whenTime = SystemClock.uptimeMillis()
        val evDownPause =
            KeyEvent(whenTime, whenTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0)
        val evUpPause = KeyEvent.changeAction(evDownPause, KeyEvent.ACTION_UP)
        val evDownPlay =
            KeyEvent(whenTime, whenTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
        val evUpPlay = KeyEvent.changeAction(evDownPlay, KeyEvent.ACTION_UP)
        mHandler.post { controller.dispatchMediaButtonEvent(evDownPause) }
        mHandler.postDelayed({ controller.dispatchMediaButtonEvent(evUpPause) }, 20)
        mHandler.postDelayed({ controller.dispatchMediaButtonEvent(evDownPlay) }, 1000)
        mHandler.postDelayed({ controller.dispatchMediaButtonEvent(evUpPlay) }, 1020)
    }

    fun getMediaControllerPlaybackState(controller: MediaController?): Int {
        return controller?.playbackState?.state ?: PlaybackState.STATE_NONE
    }
}
