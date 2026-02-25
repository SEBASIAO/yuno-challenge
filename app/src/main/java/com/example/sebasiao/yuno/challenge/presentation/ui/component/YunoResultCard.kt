package com.example.sebasiao.yuno.challenge.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

@Composable
fun YunoResultCard(
    result: AuthenticationResult,
    modifier: Modifier = Modifier
) {
    YunoCard(modifier = modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Authentication Result",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(status = result.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Action:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.decision.action.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Risk info
            Text(
                text = "Risk Assessment",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val assessment = result.decision.riskAssessment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${assessment.score}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Risk Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RiskLevelChip(riskLevel = assessment.riskLevel)
                }
            }

            // Risk factors
            if (assessment.factorResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Risk Factors",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                assessment.factorResults.forEach { factor ->
                    RiskFactorRow(factor = factor)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: AuthenticationStatus) {
    val containerColor = when (status) {
        AuthenticationStatus.AUTHENTICATED_FRICTIONLESS,
        AuthenticationStatus.AUTHENTICATED_CHALLENGE -> MaterialTheme.colorScheme.primaryContainer

        AuthenticationStatus.CHALLENGE_FAILED,
        AuthenticationStatus.ABANDONED -> MaterialTheme.colorScheme.errorContainer

        AuthenticationStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
    }
    val labelColor = when (status) {
        AuthenticationStatus.AUTHENTICATED_FRICTIONLESS,
        AuthenticationStatus.AUTHENTICATED_CHALLENGE -> MaterialTheme.colorScheme.onPrimaryContainer

        AuthenticationStatus.CHALLENGE_FAILED,
        AuthenticationStatus.ABANDONED -> MaterialTheme.colorScheme.onErrorContainer

        AuthenticationStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = formatStatusName(status),
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}

@Composable
private fun RiskLevelChip(riskLevel: RiskLevel) {
    val containerColor = when (riskLevel) {
        RiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
        RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
        RiskLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
    }
    val labelColor = when (riskLevel) {
        RiskLevel.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
        RiskLevel.HIGH -> MaterialTheme.colorScheme.onErrorContainer
        RiskLevel.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = riskLevel.name,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}

@Composable
private fun RiskFactorRow(factor: RiskFactorResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = factor.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = factor.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Score: ${factor.score}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Weight: ${String.format("%.1f", factor.weight)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatStatusName(status: AuthenticationStatus): String {
    return status.name.lowercase()
        .replace('_', ' ')
        .split(' ')
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

@Preview(showBackground = true)
@Composable
private fun YunoResultCardPreview() {
    AppTheme {
        YunoResultCard(
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
        )
    }
}
