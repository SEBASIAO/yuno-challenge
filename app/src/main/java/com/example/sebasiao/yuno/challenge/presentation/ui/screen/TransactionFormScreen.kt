package com.example.sebasiao.yuno.challenge.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoButton
import com.example.sebasiao.yuno.challenge.presentation.ui.component.YunoTopBar
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.TransactionFormViewModel
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel

@Composable
fun TransactionFormScreen(
    viewModel: TransactionFormViewModel,
    onBackClick: () -> Unit,
    onSubmit: (TransactionFormViewModel.UiState) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionFormContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onSubmit = { onSubmit(state) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormContent(
    state: TransactionFormViewModel.UiState,
    onEvent: (TransactionFormViewModel.Event) -> Unit,
    onBackClick: () -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        topBar = {
            YunoTopBar(
                title = "Custom Transaction",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onEvent(TransactionFormViewModel.Event.AmountChanged(it)) },
                label = { Text("Amount (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = state.amountError != null,
                supportingText = state.amountError?.let { error ->
                    { Text(text = error) }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.merchantName,
                onValueChange = {
                    onEvent(TransactionFormViewModel.Event.MerchantNameChanged(it))
                },
                label = { Text("Merchant Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.cardLast4,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        onEvent(TransactionFormViewModel.Event.CardLast4Changed(it))
                    }
                },
                label = { Text("Card Last 4 Digits") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trust Level Dropdown
            var trustExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = trustExpanded,
                onExpandedChange = { trustExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.trustLevel.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Customer Trust Level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = trustExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = trustExpanded,
                    onDismissRequest = { trustExpanded = false }
                ) {
                    CustomerTrustLevel.entries.forEach { level ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = level.name.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            onClick = {
                                onEvent(
                                    TransactionFormViewModel.Event.TrustLevelChanged(level)
                                )
                                trustExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            YunoButton(
                text = if (state.isProcessing) "Processing..." else "Authenticate",
                onClick = {
                    onEvent(TransactionFormViewModel.Event.Submit)
                    val amountValid = state.amount.isNotBlank() &&
                        state.amount.toDoubleOrNull()?.let { it > 0 } == true
                    if (amountValid) {
                        onSubmit()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isProcessing
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionFormContentPreview() {
    AppTheme {
        TransactionFormContent(
            state = TransactionFormViewModel.UiState(),
            onEvent = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionFormContentWithErrorPreview() {
    AppTheme {
        TransactionFormContent(
            state = TransactionFormViewModel.UiState(
                amount = "abc",
                amountError = "Enter a valid positive amount"
            ),
            onEvent = {},
            onBackClick = {},
            onSubmit = {}
        )
    }
}
