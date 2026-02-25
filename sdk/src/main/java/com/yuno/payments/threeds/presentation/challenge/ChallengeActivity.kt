package com.yuno.payments.threeds.presentation.challenge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yuno.payments.threeds.presentation.theme.ThreeDSTheme

internal class ChallengeActivity : ComponentActivity() {

    private lateinit var viewModel: ChallengeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val merchantName = intent.getStringExtra(EXTRA_MERCHANT_NAME).orEmpty()
        val amount = intent.getStringExtra(EXTRA_AMOUNT).orEmpty()
        val cardLast4 = intent.getStringExtra(EXTRA_CARD_LAST4).orEmpty()
        val validOtp = intent.getStringExtra(EXTRA_VALID_OTP).orEmpty()

        viewModel = ChallengeViewModel(
            savedStateHandle = SavedStateHandle(),
            validOtp = validOtp,
            merchantName = merchantName,
            amount = amount,
            cardLast4 = cardLast4
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleAbandonment()
            }
        })

        setContent {
            ThreeDSTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ChallengeScreen(
                    state = state,
                    onEvent = { event ->
                        when (event) {
                            is ChallengeEvent.Dismiss -> handleAbandonment()
                            is ChallengeEvent.Finish -> handleFinish()
                            else -> viewModel.onEvent(event)
                        }
                    }
                )
            }
        }
    }

    private fun handleAbandonment() {
        val abandonmentInfo = viewModel.getAbandonmentInfo()
        val resultIntent = Intent().apply {
            putExtra(EXTRA_WAS_ABANDONED, true)
            putExtra(EXTRA_ABANDONMENT_TIME_SPENT, abandonmentInfo.timeSpentMillis)
            putExtra(EXTRA_ABANDONMENT_OTP_ATTEMPTS, abandonmentInfo.otpAttemptsBeforeAbandon)
        }
        setResult(Activity.RESULT_CANCELED, resultIntent)
        finish()
    }

    private fun handleFinish() {
        val currentState = viewModel.uiState.value
        val isSuccess = currentState is ChallengeUiState.Result && currentState.isSuccess
        val resultIntent = Intent().apply {
            putExtra(EXTRA_RESULT_SUCCESS, isSuccess)
            putExtra(EXTRA_WAS_ABANDONED, false)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val EXTRA_MERCHANT_NAME = "merchant_name"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CARD_LAST4 = "card_last4"
        const val EXTRA_VALID_OTP = "valid_otp"
        const val EXTRA_RESULT_SUCCESS = "result_success"
        const val EXTRA_ABANDONMENT_TIME_SPENT = "abandonment_time_spent"
        const val EXTRA_ABANDONMENT_OTP_ATTEMPTS = "abandonment_otp_attempts"
        const val EXTRA_WAS_ABANDONED = "was_abandoned"

        fun createIntent(
            context: Context,
            merchantName: String,
            amount: String,
            cardLast4: String,
            validOtp: String
        ): Intent = Intent(context, ChallengeActivity::class.java).apply {
            putExtra(EXTRA_MERCHANT_NAME, merchantName)
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_CARD_LAST4, cardLast4)
            putExtra(EXTRA_VALID_OTP, validOtp)
        }
    }
}
