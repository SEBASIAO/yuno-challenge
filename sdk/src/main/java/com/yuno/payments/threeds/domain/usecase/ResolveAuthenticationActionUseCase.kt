package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskPolicy

internal class ResolveAuthenticationActionUseCase {
    operator fun invoke(
        riskAssessment: RiskAssessment,
        policy: RiskPolicy
    ): AuthenticationDecision = AuthenticationDecision(
        riskAssessment = riskAssessment,
        action = policy.actionFor(riskAssessment.riskLevel),
        policyApplied = policy
    )
}
