package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository

internal class VelocityRiskFactor(
    private val velocityRepository: TransactionVelocityRepository,
    private val windowMillis: Long = 300_000L
) : RiskFactor {

    override val name: String = "Transaction Velocity"
    override val weight: Double = 0.30

    override suspend fun evaluate(transaction: Transaction): RiskFactorResult {
        val velocity = velocityRepository.getVelocity(windowMillis)
        val score = when {
            velocity.transactionsInWindow <= 1 -> 5
            velocity.transactionsInWindow == 2 -> 40
            velocity.transactionsInWindow == 3 -> 75
            else -> 100
        }
        val description =
            "${velocity.transactionsInWindow} transactions in last ${windowMillis / 60000} minutes"
        return RiskFactorResult(
            name = name,
            score = score,
            weight = weight,
            description = description
        )
    }
}
