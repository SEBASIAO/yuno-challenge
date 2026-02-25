package com.yuno.payments.threeds.presentation.challenge

internal sealed interface ChallengeUiState {
    val isProcessing: Boolean get() = false

    data class ShowingTransaction(
        val merchantName: String,
        val amount: String,
        val cardLast4: String
    ) : ChallengeUiState

    data class EnteringOtp(
        val merchantName: String,
        val amount: String,
        val cardLast4: String,
        val otp: String = "",
        val otpError: String? = null,
        val otpAttempts: Int = 0,
        override val isProcessing: Boolean = false
    ) : ChallengeUiState

    data class Verifying(
        val merchantName: String,
        val amount: String
    ) : ChallengeUiState {
        override val isProcessing: Boolean = true
    }

    data class Result(
        val isSuccess: Boolean,
        val message: String
    ) : ChallengeUiState
}
