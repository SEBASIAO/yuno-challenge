package com.yuno.payments.threeds.presentation.challenge

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChallengeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val merchantName = "Test Merchant"
    private val amount = "$99.99"
    private val cardLast4 = "4242"
    private val validOtp = "123456"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        verificationDelayMillis: Long = 0L
    ): ChallengeViewModel = ChallengeViewModel(
        savedStateHandle = savedStateHandle,
        validOtp = validOtp,
        merchantName = merchantName,
        amount = amount,
        cardLast4 = cardLast4,
        verificationDelayMillis = verificationDelayMillis
    )

    @Test
    fun initialState_isShowingTransaction() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ChallengeUiState.ShowingTransaction)
            val showing = state as ChallengeUiState.ShowingTransaction
            assertEquals(merchantName, showing.merchantName)
            assertEquals(amount, showing.amount)
            assertEquals(cardLast4, showing.cardLast4)
        }
    }

    @Test
    fun onProceedToOtp_transitionsToEnteringOtp() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial ShowingTransaction

            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            val state = awaitItem()
            assertTrue(state is ChallengeUiState.EnteringOtp)
            val entering = state as ChallengeUiState.EnteringOtp
            assertEquals(merchantName, entering.merchantName)
            assertEquals(amount, entering.amount)
            assertEquals(cardLast4, entering.cardLast4)
            assertEquals("", entering.otp)
            assertNull(entering.otpError)
            assertEquals(0, entering.otpAttempts)
        }
    }

    @Test
    fun onOtpChanged_updatesOtpInState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial
            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // entering otp

            viewModel.onEvent(ChallengeEvent.OtpChanged("123"))
            val state = awaitItem() as ChallengeUiState.EnteringOtp
            assertEquals("123", state.otp)
            assertNull(state.otpError)
        }
    }

    @Test
    fun onOtpChanged_limitsTo6Characters() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial
            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // entering otp

            viewModel.onEvent(ChallengeEvent.OtpChanged("1234567890"))
            // Should not emit a new state since the input exceeds 6 chars
            expectNoEvents()
        }
    }

    @Test
    fun onSubmitOtp_withCorrectOtp_transitionsToVerifyingThenResult() = runTest {
        val viewModel = createViewModel(verificationDelayMillis = 0L)

        viewModel.uiState.test {
            awaitItem() // initial ShowingTransaction

            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // EnteringOtp

            viewModel.onEvent(ChallengeEvent.OtpChanged(validOtp))
            awaitItem() // otp updated

            viewModel.onEvent(ChallengeEvent.SubmitOtp)

            // Should transition to processing then Verifying
            val processingOrVerifying = awaitItem()
            if (processingOrVerifying is ChallengeUiState.EnteringOtp) {
                // isProcessing = true state emitted first
                assertTrue(processingOrVerifying.isProcessing)
                val verifying = awaitItem()
                assertTrue(verifying is ChallengeUiState.Verifying)
            } else {
                assertTrue(processingOrVerifying is ChallengeUiState.Verifying)
            }

            // Advance coroutines for the delay
            testScheduler.advanceUntilIdle()

            val result = awaitItem()
            assertTrue(result is ChallengeUiState.Result)
            val resultState = result as ChallengeUiState.Result
            assertTrue(resultState.isSuccess)
            assertEquals("Verification successful", resultState.message)
        }
    }

    @Test
    fun onSubmitOtp_withIncorrectOtp_showsErrorAndReturnsToEnteringOtp() = runTest {
        val viewModel = createViewModel(verificationDelayMillis = 0L)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // entering otp

            viewModel.onEvent(ChallengeEvent.OtpChanged("999999"))
            awaitItem() // otp updated

            viewModel.onEvent(ChallengeEvent.SubmitOtp)

            // Consume intermediate states (processing + verifying)
            var state = awaitItem()
            if (state is ChallengeUiState.EnteringOtp && state.isProcessing) {
                state = awaitItem() // Verifying
            }
            if (state is ChallengeUiState.Verifying) {
                testScheduler.advanceUntilIdle()
                state = awaitItem()
            }

            // Should return to EnteringOtp with error
            assertTrue(state is ChallengeUiState.EnteringOtp)
            val entering = state as ChallengeUiState.EnteringOtp
            assertEquals("", entering.otp)
            assertEquals("Incorrect code. Please try again.", entering.otpError)
            assertEquals(1, entering.otpAttempts)
        }
    }

    @Test
    fun onSubmitOtp_withShortOtp_showsLengthError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // entering otp

            viewModel.onEvent(ChallengeEvent.OtpChanged("123"))
            awaitItem() // otp = "123"

            viewModel.onEvent(ChallengeEvent.SubmitOtp)
            val state = awaitItem() as ChallengeUiState.EnteringOtp
            assertEquals("Please enter 6 digits", state.otpError)
            assertEquals("123", state.otp) // OTP not cleared
        }
    }

    @Test
    fun onSubmitOtp_whileProcessing_isIgnored() = runTest {
        // Use a long delay so we can verify the double-tap is ignored
        val viewModel = createViewModel(verificationDelayMillis = 5000L)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(ChallengeEvent.ProceedToOtp)
            awaitItem() // entering otp

            viewModel.onEvent(ChallengeEvent.OtpChanged(validOtp))
            awaitItem() // otp updated

            // First submit
            viewModel.onEvent(ChallengeEvent.SubmitOtp)

            // Consume intermediate state(s)
            var state = awaitItem()
            if (state is ChallengeUiState.EnteringOtp && state.isProcessing) {
                state = awaitItem() // Verifying
            }
            assertTrue(state is ChallengeUiState.Verifying)

            // Second submit while still processing - should be ignored
            viewModel.onEvent(ChallengeEvent.SubmitOtp)
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAbandonmentInfo_calculatesTimeSpent() = runTest {
        val knownStartTime = 1000L
        val savedStateHandle = SavedStateHandle(
            mapOf("challenge_started_at" to knownStartTime)
        )
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)

        val info = viewModel.getAbandonmentInfo()

        // The abandonedAt should be close to current time
        assertTrue(info.abandonedAt > knownStartTime)
        // timeSpentMillis should be abandonedAt - startTime
        assertEquals(info.abandonedAt - knownStartTime, info.timeSpentMillis)
        assertEquals(0, info.otpAttemptsBeforeAbandon)
    }

    @Test
    fun savedStateHandle_restoresPartialOtp() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "saved_otp" to "456",
                "otp_attempts" to 2
            )
        )
        val viewModel = createViewModel(savedStateHandle = savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ChallengeUiState.EnteringOtp)
            val entering = state as ChallengeUiState.EnteringOtp
            assertEquals("456", entering.otp)
            assertEquals(2, entering.otpAttempts)
        }
    }
}
