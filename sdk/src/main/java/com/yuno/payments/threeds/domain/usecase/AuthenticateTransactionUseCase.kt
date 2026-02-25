package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.RiskPolicy
import com.yuno.payments.threeds.domain.model.Transaction

internal class AuthenticateTransactionUseCase(
    private val evaluateRisk: EvaluateTransactionRiskUseCase,
    private val resolveAction: ResolveAuthenticationActionUseCase,
    private val recordVelocity: RecordTransactionVelocityUseCase
) {
    suspend operator fun invoke(
        transaction: Transaction,
        policy: RiskPolicy
    ): AuthenticationDecision {
        recordVelocity(transaction.id)
        val assessment = evaluateRisk(transaction)
        return resolveAction(assessment, policy)
    }
}
