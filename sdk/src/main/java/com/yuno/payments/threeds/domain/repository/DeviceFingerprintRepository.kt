package com.yuno.payments.threeds.domain.repository

import com.yuno.payments.threeds.domain.model.DeviceFingerprint

internal interface DeviceFingerprintRepository {
    suspend fun getFingerprint(): DeviceFingerprint
    suspend fun recordSuccessfulAuth()
    suspend fun getSuccessfulAuthCount(): Int
}
