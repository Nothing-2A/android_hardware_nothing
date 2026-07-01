/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.nothing.dirac

import android.media.audiofx.AudioEffect
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class DiracSound(priority: Int, audioSession: Int) {

    companion object {
        private const val DIRAC_SOUND_PARAM_EQ_LEVEL = 2
        private const val DIRAC_SOUND_PARAM_VOLUME = 5
        private const val DIRAC_SOUND_PARAM_ENABLE = 6

        // AudioEffect.EFFECT_TYPE_NULL — hardcoded since it's @hide
        private val EFFECT_TYPE_NULL: UUID = UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210")
        private val EFFECT_TYPE_DIRAC_SOUND: UUID =
            UUID.fromString("ae737c63-f2c0-5457-909e-1e940c91b67b")

        private val ctor by lazy {
            AudioEffect::class.java.getDeclaredConstructor(
                UUID::class.java, UUID::class.java,
                Int::class.java, Int::class.java
            ).also { it.isAccessible = true }
        }

        private val mCheckStatus by lazy {
            AudioEffect::class.java.getDeclaredMethod("checkStatus", Int::class.java)
                .also { it.isAccessible = true }
        }

        private val mSetParamIntInt by lazy {
            AudioEffect::class.java.getDeclaredMethod(
                "setParameter", Int::class.java, Int::class.java
            ).also { it.isAccessible = true }
        }

        private val mSetParamIntArrayByteArray by lazy {
            AudioEffect::class.java.getDeclaredMethod(
                "setParameter", IntArray::class.java, ByteArray::class.java
            ).also { it.isAccessible = true }
        }
    }

    private val fx: AudioEffect =
        ctor.newInstance(
            EFFECT_TYPE_NULL,
            EFFECT_TYPE_DIRAC_SOUND,
            priority,
            audioSession
        ) as AudioEffect

    private fun checkStatus(status: Int) {
        mCheckStatus.invoke(fx, status)
    }

    private fun setParameter(param: Int, value: Int): Int =
        mSetParamIntInt.invoke(fx, param, value) as Int

    private fun setParameter(params: IntArray, value: ByteArray): Int =
        mSetParamIntArrayByteArray.invoke(fx, params, value) as Int

    fun setEnabled(enable: Boolean) {
        checkStatus(setParameter(DIRAC_SOUND_PARAM_ENABLE, if (enable) 1 else 0))
        fx.enabled = enable
    }

    fun setVolume(volume: Int) {
        checkStatus(setParameter(DIRAC_SOUND_PARAM_VOLUME, volume))
    }

    fun setLevel(band: Int, level: Float) {
        val value = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(level).array()
        checkStatus(setParameter(intArrayOf(DIRAC_SOUND_PARAM_EQ_LEVEL, band), value))
    }

    fun release() {
        fx.release()
    }
}
