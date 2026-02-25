package com.yuno.payments.threeds.presentation.challenge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yuno.payments.threeds.presentation.component.ThreeDSOtpField
import com.yuno.payments.threeds.presentation.component.ThreeDSProgressIndicator
import com.yuno.payments.threeds.presentation.component.ThreeDSResultBanner
import com.yuno.payments.threeds.presentation.component.ThreeDSTransactionSummary
import com.yuno.payments.threeds.presentation.theme.ThreeDSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChallengeScreen(
    state: ChallengeUiState,
    onEvent: (ChallengeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "3DS Verification")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (state) {
                is ChallengeUiState.ShowingTransaction -> {
                    ShowingTransactionContent(
                        state = state,
                        onProceed = { onEvent(ChallengeEvent.ProceedToOtp) }
                    )
                }

                is ChallengeUiState.EnteringOtp -> {
                    EnteringOtpContent(
                        state = state,
                        onOtpChanged = { onEvent(ChallengeEvent.OtpChanged(it)) },
                        onSubmit = { onEvent(ChallengeEvent.SubmitOtp) }
                    )
                }

                is ChallengeUiState.Verifying -> {
                    VerifyingContent(state = state)
                }

                is ChallengeUiState.Result -> {
                    ResultContent(
                        state = state,
                        onFinish = { onEvent(ChallengeEvent.Finish) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowingTransactionContent(
    state: ChallengeUiState.ShowingTransaction,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Transaction Verification Required",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThreeDSTransactionSummary(
                merchantName = state.merchantName,
                amount = state.amount,
                cardLast4 = state.cardLast4
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You will be asked to enter a verification code sent to your registered device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Continue")
        }
    }
}

@Composable
private fun EnteringOtpContent(
    state: ChallengeUiState.EnteringOtp,
    onOtpChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThreeDSTransactionSummary(
                merchantName = state.merchantName,
                amount = state.amount,
                cardLast4 = state.cardLast4
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            ThreeDSOtpField(
                otp = state.otp,
                onOtpChanged = onOtpChanged,
                isError = state.otpError != null,
                enabled = !state.isProcessing
            )
            if (state.otpError != null) {
                Text(
                    text = state.otpError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !state.isProcessing
        ) {
            Text(text = "Verify")
        }
    }
}

@Composable
private fun VerifyingContent(state: ChallengeUiState.Verifying) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ThreeDSProgressIndicator(
            message = "Verifying payment of ${state.amount} to ${state.merchantName}..."
        )
    }
}

@Composable
private fun ResultContent(
    state: ChallengeUiState.Result,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        ThreeDSResultBanner(
            isSuccess = state.isSuccess,
            message = state.message
        )
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Done")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeScreenShowingTransactionPreview() {
    ThreeDSTheme {
        ChallengeScreen(
            state = ChallengeUiState.ShowingTransaction(
                merchantName = "Amazon Store",
                amount = "$149.99",
                cardLast4 = "4242"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeScreenEnteringOtpPreview() {
    ThreeDSTheme {
        ChallengeScreen(
            state = ChallengeUiState.EnteringOtp(
                merchantName = "Amazon Store",
                amount = "$149.99",
                cardLast4 = "4242",
                otp = "123"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeScreenVerifyingPreview() {
    ThreeDSTheme {
        ChallengeScreen(
            state = ChallengeUiState.Verifying(
                merchantName = "Amazon Store",
                amount = "$149.99"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeScreenResultSuccessPreview() {
    ThreeDSTheme {
        ChallengeScreen(
            state = ChallengeUiState.Result(
                isSuccess = true,
                message = "Verification successful"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChallengeScreenResultFailurePreview() {
    ThreeDSTheme {
        ChallengeScreen(
            state = ChallengeUiState.Result(
                isSuccess = false,
                message = "Verification failed"
            ),
            onEvent = {}
        )
    }
}
