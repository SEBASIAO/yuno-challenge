package com.example.sebasiao.yuno.challenge.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoEmptyState
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoLoadingIndicator
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoPolicyToggle
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoTopBar
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoTransactionCard
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.TransactionListViewModel
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel

@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel,
    onTransactionClick: (String) -> Unit,
    onCustomTransactionClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onTransactionClick = onTransactionClick,
        onCustomTransactionClick = onCustomTransactionClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListContent(
    state: TransactionListViewModel.UiState,
    onEvent: (TransactionListViewModel.Event) -> Unit,
    onTransactionClick: (String) -> Unit,
    onCustomTransactionClick: () -> Unit
) {
    Scaffold(
        topBar = {
            YunoTopBar(title = "3DS Transactions")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCustomTransactionClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Custom Transaction"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    YunoLoadingIndicator(message = "Loading transactions...")
                }

                state.transactions.isEmpty() -> {
                    YunoEmptyState(message = "No sample transactions available")
                }

                else -> {
                    YunoPolicyToggle(
                        activePolicy = state.activePolicy,
                        onPolicySelected = { policy ->
                            onEvent(TransactionListViewModel.Event.TogglePolicy(policy))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(
                            items = state.transactions,
                            key = { it.id }
                        ) { transaction ->
                            YunoTransactionCard(
                                transaction = transaction,
                                onClick = {
                                    onEvent(
                                        TransactionListViewModel.Event.TransactionClicked(
                                            transaction.id
                                        )
                                    )
                                    onTransactionClick(transaction.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListContentLoadingPreview() {
    AppTheme {
        TransactionListContent(
            state = TransactionListViewModel.UiState(isLoading = true),
            onEvent = {},
            onTransactionClick = {},
            onCustomTransactionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListContentEmptyPreview() {
    AppTheme {
        TransactionListContent(
            state = TransactionListViewModel.UiState(
                isLoading = false,
                transactions = emptyList()
            ),
            onEvent = {},
            onTransactionClick = {},
            onCustomTransactionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionListContentPreview() {
    AppTheme {
        TransactionListContent(
            state = TransactionListViewModel.UiState(
                isLoading = false,
                transactions = listOf(
                    SampleTransaction(
                        id = "1",
                        amount = 25.0,
                        currency = "USD",
                        merchantName = "Coffee Shop",
                        cardLast4 = "1234",
                        customerTrustLevel = CustomerTrustLevel.TRUSTED,
                        scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
                        scenarioDescription = "Low risk frictionless flow"
                    ),
                    SampleTransaction(
                        id = "2",
                        amount = 500.0,
                        currency = "USD",
                        merchantName = "Electronics Store",
                        cardLast4 = "5678",
                        customerTrustLevel = CustomerTrustLevel.RETURNING,
                        scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
                        scenarioDescription = "Medium risk challenge flow"
                    )
                )
            ),
            onEvent = {},
            onTransactionClick = {},
            onCustomTransactionClick = {}
        )
    }
}
