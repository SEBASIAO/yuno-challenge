package com.example.sebasiao.yuno.challenge.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoButton
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoCard
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoLoadingIndicator
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoResultCard
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoTopBar
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.ResultViewModel
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ResultContent(
        state = state,
        onBackClick = onBackClick
    )
}

@Composable
fun ResultContent(
    state: ResultViewModel.UiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            YunoTopBar(
                title = "Result",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        when (state) {
            is ResultViewModel.UiState.Loading -> {
                YunoLoadingIndicator(
                    message = "Loading result...",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is ResultViewModel.UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Transaction info card
                    state.transaction?.let { transaction ->
                        TransactionInfoSection(transaction = transaction)
                    }

                    // Authentication result card
                    YunoResultCard(result = state.result)

                    Spacer(modifier = Modifier.height(16.dp))

                    YunoButton(
                        text = "Back to Transactions",
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionInfoSection(transaction: SampleTransaction) {
    YunoCard(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Transaction Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Merchant", value = transaction.merchantName)
            InfoRow(
                label = "Amount",
                value = "${transaction.currency} ${String.format("%.2f", transaction.amount)}"
            )
            InfoRow(label = "Card", value = "****${transaction.cardLast4}")
            InfoRow(
                label = "Trust Level",
                value = transaction.customerTrustLevel.name.lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
            InfoRow(label = "Scenario", value = transaction.scenarioDescription)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentLoadingPreview() {
    AppTheme {
        ResultContent(
            state = ResultViewModel.UiState.Loading,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentSuccessPreview() {
    AppTheme {
        ResultContent(
            state = ResultViewModel.UiState.Success(
                transaction = SampleTransaction(
                    id = "1",
                    amount = 25.0,
                    currency = "USD",
                    merchantName = "Coffee Shop",
                    cardLast4 = "1234",
                    customerTrustLevel = CustomerTrustLevel.TRUSTED,
                    scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
                    scenarioDescription = "Low risk frictionless flow"
                ),
                result = AuthenticationResult(
                    status = AuthenticationStatus.AUTHENTICATED_FRICTIONLESS,
                    decision = AuthenticationDecision(
                        riskAssessment = RiskAssessment(
                            score = 15,
                            riskLevel = RiskLevel.LOW,
                            factorResults = listOf(
                                RiskFactorResult("Amount", 10, 0.3, "Low amount"),
                                RiskFactorResult("Trust", 5, 0.4, "Trusted customer")
                            )
                        ),
                        action = AuthenticationAction.FRICTIONLESS,
                        policyApplied = RiskPolicy.default()
                    )
                )
            ),
            onBackClick = {}
        )
    }
}
