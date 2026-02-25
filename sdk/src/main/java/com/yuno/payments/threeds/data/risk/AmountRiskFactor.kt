package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.Transaction

internal class AmountRiskFactor : RiskFactor {

    override val name: String = "Transaction Amount"
    override val weight: Double = 0.35

    override suspend fun evaluate(transaction: Transaction): RiskFactorResult {
        val score = when {
            transaction.amount < 50.0 -> 10
            transaction.amount < 200.0 -> 50
            transaction.amount < 400.0 -> 75
            else -> 95
        }
        val description = when {
            transaction.amount < 50.0 -> "Low amount transaction"
            transaction.amount < 200.0 -> "Moderate amount transaction"
            transaction.amount < 400.0 -> "High amount transaction"
            else -> "Very high amount transaction"
        }
        return RiskFactorResult(
            name = name,
            score = score,
            weight = weight,
            description = description
        )
    }
}
