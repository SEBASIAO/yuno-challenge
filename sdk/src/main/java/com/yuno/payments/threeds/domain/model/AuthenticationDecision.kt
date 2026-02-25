package com.yuno.payments.threeds.domain.model

data class AuthenticationDecision(
    val riskAssessment: RiskAssessment,
    val action: AuthenticationAction,
    val policyApplied: RiskPolicy
)
