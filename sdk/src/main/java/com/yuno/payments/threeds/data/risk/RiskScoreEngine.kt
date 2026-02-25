package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.Transaction

internal class RiskScoreEngine(
    private val factors: List<RiskFactor>
) {

    suspend fun evaluate(transaction: Transaction): RiskAssessment {
        val results = factors.map { it.evaluate(transaction) }
        val weightedScore = results.sumOf { it.score * it.weight }
        val totalWeight = results.sumOf { it.weight }
        val normalizedScore = if (totalWeight > 0) (weightedScore / totalWeight).toInt() else 0
        val clampedScore = normalizedScore.coerceIn(0, 100)
        val riskLevel = when {
            clampedScore <= 25 -> RiskLevel.LOW
            clampedScore <= 50 -> RiskLevel.MEDIUM
            clampedScore <= 75 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
        return RiskAssessment(
            score = clampedScore,
            riskLevel = riskLevel,
            factorResults = results
        )
    }
}
