package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository

internal class TrustLevelRiskFactor(
    private val fingerprintRepository: DeviceFingerprintRepository,
    private val deviceTrustThreshold: Int = 3
) : RiskFactor {

    override val name: String = "Customer Trust Level"
    override val weight: Double = 0.35

    override suspend fun evaluate(transaction: Transaction): RiskFactorResult {
        val baseScore = when (transaction.customerTrustLevel) {
            CustomerTrustLevel.NEW -> 70
            CustomerTrustLevel.RETURNING -> 40
            CustomerTrustLevel.TRUSTED -> 15
        }
        val authCount = fingerprintRepository.getSuccessfulAuthCount()
        val deviceBonus = if (authCount >= deviceTrustThreshold) -15 else 0
        val finalScore = (baseScore + deviceBonus).coerceIn(0, 100)
        val description = buildString {
            append("${transaction.customerTrustLevel} customer")
            if (deviceBonus < 0) append(" (device trust bonus: $deviceBonus)")
        }
        return RiskFactorResult(
            name = name,
            score = finalScore,
            weight = weight,
            description = description
        )
    }
}
