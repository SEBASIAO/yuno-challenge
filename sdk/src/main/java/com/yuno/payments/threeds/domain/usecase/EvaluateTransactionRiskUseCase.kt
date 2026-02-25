package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.RiskRepository

internal class EvaluateTransactionRiskUseCase(
    private val riskRepository: RiskRepository
) {
    suspend operator fun invoke(transaction: Transaction): RiskAssessment =
        riskRepository.evaluateRisk(transaction)
}
