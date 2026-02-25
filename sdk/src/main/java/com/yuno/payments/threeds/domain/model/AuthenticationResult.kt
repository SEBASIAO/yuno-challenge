package com.yuno.payments.threeds.domain.model

data class AuthenticationResult(
    val status: AuthenticationStatus,
    val decision: AuthenticationDecision,
    val challengeCompletedAt: Long? = null,
    val abandonmentInfo: AbandonmentInfo? = null
)
