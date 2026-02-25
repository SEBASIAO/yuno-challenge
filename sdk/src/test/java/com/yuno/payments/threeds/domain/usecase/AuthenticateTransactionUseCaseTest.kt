package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
import com.yuno.payments.threeds.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticateTransactionUseCaseTest {

    private val evaluateRisk: EvaluateTransactionRiskUseCase = mockk()
    private val resolveAction: ResolveAuthenticationActionUseCase = mockk()
    private val recordVelocity: RecordTransactionVelocityUseCase = mockk()

    private val useCase = AuthenticateTransactionUseCase(
        evaluateRisk = evaluateRisk,
        resolveAction = resolveAction,
        recordVelocity = recordVelocity
    )

    private val transaction = Transaction(
        id = "txn-001",
        amount = 100.0,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.NEW,
        timestamp = 1000L
    )

    private val defaultPolicy = RiskPolicy.default()

    private val lowRiskAssessment = RiskAssessment(
        score = 15,
        riskLevel = RiskLevel.LOW,
        factorResults = emptyList()
    )

    private val frictionlessDecision = AuthenticationDecision(
        riskAssessment = lowRiskAssessment,
        action = AuthenticationAction.FRICTIONLESS,
        policyApplied = defaultPolicy
    )

    @Test
    fun invoke_recordsVelocityBeforeEvaluation() = runTest {
        coEvery { recordVelocity(transaction.id) } returns Unit
        coEvery { evaluateRisk(transaction) } returns lowRiskAssessment
        every { resolveAction(lowRiskAssessment, defaultPolicy) } returns frictionlessDecision

        useCase(transaction, defaultPolicy)

        coVerifyOrder {
            recordVelocity(transaction.id)
            evaluateRisk(transaction)
        }
    }

    @Test
    fun invoke_evaluatesRiskAndAppliesPolicy() = runTest {
        val mediumAssessment = RiskAssessment(
            score = 50,
            riskLevel = RiskLevel.MEDIUM,
            factorResults = emptyList()
        )
        val challengeDecision = AuthenticationDecision(
            riskAssessment = mediumAssessment,
            action = AuthenticationAction.CHALLENGE,
            policyApplied = defaultPolicy
        )

        coEvery { recordVelocity(transaction.id) } returns Unit
        coEvery { evaluateRisk(transaction) } returns mediumAssessment
        every { resolveAction(mediumAssessment, defaultPolicy) } returns challengeDecision

        val result = useCase(transaction, defaultPolicy)

        assertEquals(challengeDecision, result)
        coVerify { evaluateRisk(transaction) }
        coVerify { resolveAction(mediumAssessment, defaultPolicy) }
    }

    @Test
    fun invoke_withLowRisk_returnsFrictionlessDecision() = runTest {
        coEvery { recordVelocity(transaction.id) } returns Unit
        coEvery { evaluateRisk(transaction) } returns lowRiskAssessment
        every { resolveAction(lowRiskAssessment, defaultPolicy) } returns frictionlessDecision

        val result = useCase(transaction, defaultPolicy)

        assertEquals(AuthenticationAction.FRICTIONLESS, result.action)
        assertEquals(lowRiskAssessment, result.riskAssessment)
        assertEquals(defaultPolicy, result.policyApplied)
    }
}
