package com.example.sebasiao.yuno.challenge.presentation.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.TransactionListViewModel

@Composable
fun YunoPolicyToggle(
    activePolicy: TransactionListViewModel.PolicyOption,
    onPolicySelected: (TransactionListViewModel.PolicyOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        TransactionListViewModel.PolicyOption.entries.forEach { policy ->
            val isSelected = policy == activePolicy
            FilterChip(
                selected = isSelected,
                onClick = { onPolicySelected(policy) },
                label = {
                    Text(
                        text = formatPolicyName(policy),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

private fun formatPolicyName(policy: TransactionListViewModel.PolicyOption): String {
    return when (policy) {
        TransactionListViewModel.PolicyOption.POLICY_A -> "Policy A (Default)"
        TransactionListViewModel.PolicyOption.POLICY_B -> "Policy B (Strict)"
    }
}

@Preview(showBackground = true)
@Composable
private fun YunoPolicyTogglePolicyAPreview() {
    AppTheme {
        YunoPolicyToggle(
            activePolicy = TransactionListViewModel.PolicyOption.POLICY_A,
            onPolicySelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YunoPolicyTogglePolicyBPreview() {
    AppTheme {
        YunoPolicyToggle(
            activePolicy = TransactionListViewModel.PolicyOption.POLICY_B,
            onPolicySelected = {}
        )
    }
}
