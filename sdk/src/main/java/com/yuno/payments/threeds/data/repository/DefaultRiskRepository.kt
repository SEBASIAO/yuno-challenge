package com.yuno.payments.threeds.data.repository

import com.yuno.payments.threeds.data.risk.RiskScoreEngine
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.RiskRepository

internal class DefaultRiskRepository(
    private val engine: RiskScoreEngine
) : RiskRepository {

    override suspend fun evaluateRisk(transaction: Transaction): RiskAssessment =
        engine.evaluate(transaction)
}
