package com.yuno.payments.threeds.domain.model

data class AbandonmentInfo(
    val abandonedAt: Long,
    val timeSpentMillis: Long,
    val otpAttemptsBeforeAbandon: Int
)
