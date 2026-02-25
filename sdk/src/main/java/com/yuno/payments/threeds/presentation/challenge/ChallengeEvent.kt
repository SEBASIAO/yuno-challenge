package com.yuno.payments.threeds.presentation.challenge

internal sealed interface ChallengeEvent {
    data object ProceedToOtp : ChallengeEvent
    data class OtpChanged(val otp: String) : ChallengeEvent
    data object SubmitOtp : ChallengeEvent
    data object Dismiss : ChallengeEvent
    data object Finish : ChallengeEvent
}
