package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.AbandonmentInfo

internal class TrackChallengeAbandonmentUseCase {
    operator fun invoke(
        challengeStartedAt: Long,
        abandonedAt: Long,
        otpAttempts: Int
    ): AbandonmentInfo = AbandonmentInfo(
        abandonedAt = abandonedAt,
        timeSpentMillis = abandonedAt - challengeStartedAt,
        otpAttemptsBeforeAbandon = otpAttempts
    )
}
