package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.Transaction

internal interface RiskFactor {
    val name: String
    val weight: Double
    suspend fun evaluate(transaction: Transaction): RiskFactorResult
}
