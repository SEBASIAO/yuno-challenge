package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveAuthenticationActionUseCaseTest {

    private val useCase = ResolveAuthenticationActionUseCase()

    private fun createAssessment(riskLevel: RiskLevel, score: Int = 50) = RiskAssessment(
        score = score,
        riskLevel = riskLevel,
        factorResults = emptyList()
    )

    @Test
    fun invoke_withLowRiskDefaultPolicy_returnsFrictionless() {
        val assessment = createAssessment(RiskLevel.LOW, score = 15)
        val policy = RiskPolicy.default()

        val decision = useCase(assessment, policy)

        assertEquals(AuthenticationAction.FRICTIONLESS, decision.action)
    }

    @Test
    fun invoke_withMediumRiskDefaultPolicy_returnsChallenge() {
        val assessment = createAssessment(RiskLevel.MEDIUM, score = 45)
        val policy = RiskPolicy.default()

        val decision = useCase(assessment, policy)

        assertEquals(AuthenticationAction.CHALLENGE, decision.action)
    }

    @Test
    fun invoke_withMediumRiskCustomPolicy_returnsFrictionless() {
        val assessment = createAssessment(RiskLevel.MEDIUM, score = 45)
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS
            )
        )

        val decision = useCase(assessment, customPolicy)

        assertEquals(AuthenticationAction.FRICTIONLESS, decision.action)
    }

    @Test
    fun invoke_includesAppliedPolicyInDecision() {
        val assessment = createAssessment(RiskLevel.HIGH, score = 75)
        val policy = RiskPolicy.default()

        val decision = useCase(assessment, policy)

        assertEquals(policy, decision.policyApplied)
        assertEquals(assessment, decision.riskAssessment)
        assertEquals(AuthenticationAction.CHALLENGE, decision.action)
    }
}
