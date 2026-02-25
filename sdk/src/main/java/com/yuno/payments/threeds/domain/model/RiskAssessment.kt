package com.yuno.payments.threeds.domain.model

data class RiskAssessment(
    val score: Int,
    val riskLevel: RiskLevel,
    val factorResults: List<RiskFactorResult>
)
