package com.yuno.payments.threeds.presentation.challenge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuno.payments.threeds.domain.model.AbandonmentInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ChallengeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val validOtp: String,
    private val merchantName: String,
    private val amount: String,
    private val cardLast4: String,
    private val verificationDelayMillis: Long = DEFAULT_VERIFICATION_DELAY_MILLIS
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeUiState>(
        ChallengeUiState.ShowingTransaction(merchantName, amount, cardLast4)
    )
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private val challengeStartedAt: Long = savedStateHandle.get<Long>(KEY_CHALLENGE_STARTED)
        ?: System.currentTimeMillis().also { savedStateHandle[KEY_CHALLENGE_STARTED] = it }

    private var otpAttempts: Int = savedStateHandle.get<Int>(KEY_OTP_ATTEMPTS) ?: 0

    init {
        val savedOtp = savedStateHandle.get<String>(KEY_SAVED_OTP)
        if (savedOtp != null) {
            _uiState.update {
                ChallengeUiState.EnteringOtp(
                    merchantName = merchantName,
                    amount = amount,
                    cardLast4 = cardLast4,
                    otp = savedOtp,
                    otpAttempts = otpAttempts
                )
            }
        }
    }

    fun onEvent(event: ChallengeEvent) {
        when (event) {
            is ChallengeEvent.ProceedToOtp -> handleProceedToOtp()
            is ChallengeEvent.OtpChanged -> handleOtpChanged(event.otp)
            is ChallengeEvent.SubmitOtp -> handleSubmitOtp()
            is ChallengeEvent.Dismiss -> { /* handled by Activity */ }
            is ChallengeEvent.Finish -> { /* handled by Activity */ }
        }
    }

    private fun handleProceedToOtp() {
        _uiState.update {
            ChallengeUiState.EnteringOtp(
                merchantName = merchantName,
                amount = amount,
                cardLast4 = cardLast4
            )
        }
    }

    private fun handleOtpChanged(otp: String) {
        if (otp.length <= MAX_OTP_LENGTH) {
            savedStateHandle[KEY_SAVED_OTP] = otp
            _uiState.update { current ->
                if (current is ChallengeUiState.EnteringOtp) {
                    current.copy(otp = otp, otpError = null)
                } else {
                    current
                }
            }
        }
    }

    private fun handleSubmitOtp() {
        val current = _uiState.value
        if (current !is ChallengeUiState.EnteringOtp || current.isProcessing) return

        if (current.otp.length != MAX_OTP_LENGTH) {
            _uiState.update {
                current.copy(otpError = ERROR_OTP_LENGTH)
            }
            return
        }

        val submittedOtp = current.otp
        _uiState.update { current.copy(isProcessing = true) }
        otpAttempts++
        savedStateHandle[KEY_OTP_ATTEMPTS] = otpAttempts

        viewModelScope.launch {
            _uiState.update {
                ChallengeUiState.Verifying(
                    merchantName = merchantName,
                    amount = amount
                )
            }
            delay(verificationDelayMillis)

            if (submittedOtp == validOtp) {
                _uiState.update {
                    ChallengeUiState.Result(
                        isSuccess = true,
                        message = MESSAGE_VERIFICATION_SUCCESS
                    )
                }
            } else {
                _uiState.update {
                    ChallengeUiState.EnteringOtp(
                        merchantName = merchantName,
                        amount = amount,
                        cardLast4 = cardLast4,
                        otp = "",
                        otpError = ERROR_INCORRECT_CODE,
                        otpAttempts = otpAttempts
                    )
                }
            }
        }
    }

    fun getAbandonmentInfo(): AbandonmentInfo {
        val now = System.currentTimeMillis()
        return AbandonmentInfo(
            abandonedAt = now,
            timeSpentMillis = now - challengeStartedAt,
            otpAttemptsBeforeAbandon = otpAttempts
        )
    }

    internal companion object {
        const val KEY_CHALLENGE_STARTED = "challenge_started_at"
        const val KEY_SAVED_OTP = "saved_otp"
        const val KEY_OTP_ATTEMPTS = "otp_attempts"

        private const val MAX_OTP_LENGTH = 6
        private const val DEFAULT_VERIFICATION_DELAY_MILLIS = 2000L
        private const val ERROR_OTP_LENGTH = "Please enter 6 digits"
        private const val ERROR_INCORRECT_CODE = "Incorrect code. Please try again."
        private const val MESSAGE_VERIFICATION_SUCCESS = "Verification successful"
    }
}
