package com.yuno.payments.threeds.data.repository

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class SharedPrefsDeviceFingerprintRepositoryTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sut: SharedPrefsDeviceFingerprintRepository

    @Before
    fun setUp() {
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        sut = SharedPrefsDeviceFingerprintRepository(sharedPreferences)
    }

    @Test
    fun getFingerprint_firstCall_generatesDeviceId() = runTest {
        every { sharedPreferences.getString("threeds_device_id", null) } returns null
        every { sharedPreferences.getInt("threeds_auth_count", 0) } returns 0
        every { sharedPreferences.getLong("threeds_last_auth", -1L) } returns -1L

        val fingerprint = sut.getFingerprint()

        assertNotNull(fingerprint.deviceId)
        assertTrue(fingerprint.deviceId.isNotEmpty())
        verify { editor.putString("threeds_device_id", any()) }
    }

    @Test
    fun getFingerprint_secondCall_returnsSameDeviceId() = runTest {
        val existingId = "existing-device-id-123"
        every { sharedPreferences.getString("threeds_device_id", null) } returns existingId
        every { sharedPreferences.getInt("threeds_auth_count", 0) } returns 2
        every { sharedPreferences.getLong("threeds_last_auth", -1L) } returns 1000L

        val fingerprint = sut.getFingerprint()

        assertEquals(existingId, fingerprint.deviceId)
        assertEquals(2, fingerprint.successfulAuthCount)
        assertEquals(1000L, fingerprint.lastAuthTimestamp)
    }

    @Test
    fun recordSuccessfulAuth_incrementsCount() = runTest {
        every { sharedPreferences.getInt("threeds_auth_count", 0) } returns 3

        sut.recordSuccessfulAuth()

        verify { editor.putInt("threeds_auth_count", 4) }
        verify { editor.putLong("threeds_last_auth", any()) }
        verify { editor.apply() }
    }

    @Test
    fun getSuccessfulAuthCount_returnsStoredCount() = runTest {
        every { sharedPreferences.getInt("threeds_auth_count", 0) } returns 7

        val count = sut.getSuccessfulAuthCount()

        assertEquals(7, count)
    }

    @Test
    fun getFingerprint_noLastAuth_returnsNullTimestamp() = runTest {
        every { sharedPreferences.getString("threeds_device_id", null) } returns "device-id"
        every { sharedPreferences.getInt("threeds_auth_count", 0) } returns 0
        every { sharedPreferences.getLong("threeds_last_auth", -1L) } returns -1L

        val fingerprint = sut.getFingerprint()

        assertEquals(null, fingerprint.lastAuthTimestamp)
    }
}
