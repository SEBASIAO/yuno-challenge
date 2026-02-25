package com.yuno.payments.threeds.data.repository

import android.content.SharedPreferences
import com.yuno.payments.threeds.domain.model.DeviceFingerprint
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

internal class SharedPrefsDeviceFingerprintRepository(
    private val sharedPreferences: SharedPreferences
) : DeviceFingerprintRepository {

    private val mutex = Mutex()

    override suspend fun getFingerprint(): DeviceFingerprint {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                val deviceId = sharedPreferences.getString(KEY_DEVICE_ID, null) ?: run {
                    val newId = UUID.randomUUID().toString()
                    sharedPreferences.edit().putString(KEY_DEVICE_ID, newId).apply()
                    newId
                }
                val authCount = sharedPreferences.getInt(KEY_AUTH_COUNT, 0)
                val lastAuth = sharedPreferences.getLong(KEY_LAST_AUTH, -1L).takeIf { it >= 0 }
                DeviceFingerprint(deviceId, authCount, lastAuth)
            }
        }
    }

    override suspend fun recordSuccessfulAuth() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val currentCount = sharedPreferences.getInt(KEY_AUTH_COUNT, 0)
                sharedPreferences.edit()
                    .putInt(KEY_AUTH_COUNT, currentCount + 1)
                    .putLong(KEY_LAST_AUTH, System.currentTimeMillis())
                    .apply()
            }
        }
    }

    override suspend fun getSuccessfulAuthCount(): Int {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                sharedPreferences.getInt(KEY_AUTH_COUNT, 0)
            }
        }
    }

    companion object {
        private const val KEY_DEVICE_ID = "threeds_device_id"
        private const val KEY_AUTH_COUNT = "threeds_auth_count"
        private const val KEY_LAST_AUTH = "threeds_last_auth"
    }
}
