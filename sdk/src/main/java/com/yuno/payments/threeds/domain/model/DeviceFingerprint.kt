package com.yuno.payments.threeds.domain.model

internal data class DeviceFingerprint(
    val deviceId: String,
    val successfulAuthCount: Int,
    val lastAuthTimestamp: Long?
)
