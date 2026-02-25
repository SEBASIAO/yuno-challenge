package com.yuno.payments.threeds.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackChallengeAbandonmentUseCaseTest {

    private val useCase = TrackChallengeAbandonmentUseCase()

    @Test
    fun invoke_calculatesTimeSpentCorrectly() {
        val challengeStartedAt = 1000L
        val abandonedAt = 6000L
        val otpAttempts = 2

        val result = useCase(challengeStartedAt, abandonedAt, otpAttempts)

        assertEquals(5000L, result.timeSpentMillis)
        assertEquals(abandonedAt, result.abandonedAt)
    }

    @Test
    fun invoke_preservesOtpAttemptCount() {
        val challengeStartedAt = 1000L
        val abandonedAt = 3000L
        val otpAttempts = 3

        val result = useCase(challengeStartedAt, abandonedAt, otpAttempts)

        assertEquals(3, result.otpAttemptsBeforeAbandon)
    }
}
