package com.yuno.payments.threeds.domain.repository

import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.Transaction

internal interface RiskRepository {
    suspend fun evaluateRisk(transaction: Transaction): RiskAssessment
}
