package com.example.sebasiao.yuno.challenge.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel

@Composable
fun YunoTransactionCard(
    transaction: SampleTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    YunoCard(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.merchantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${transaction.currency} ${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ****${transaction.cardLast4}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = transaction.customerTrustLevel.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = formatScenarioName(transaction.scenario),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            Text(
                text = transaction.scenarioDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatScenarioName(scenario: TransactionScenario): String {
    return scenario.name.lowercase()
        .replace('_', ' ')
        .split(' ')
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

@Preview(showBackground = true)
@Composable
private fun YunoTransactionCardPreview() {
    AppTheme {
        YunoTransactionCard(
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
            onClick = {}
        )
    }
}
