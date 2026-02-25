package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.RiskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluateTransactionRiskUseCaseTest {

    private val riskRepository: RiskRepository = mockk()
    private val useCase = EvaluateTransactionRiskUseCase(riskRepository)

    private val transaction = Transaction(
        id = "txn-001",
        amount = 100.0,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.NEW,
        timestamp = 1000L
    )

    private val expectedAssessment = RiskAssessment(
        score = 45,
        riskLevel = RiskLevel.MEDIUM,
        factorResults = listOf(
            RiskFactorResult(
                name = "amount",
                score = 45,
                weight = 1.0,
                description = "Medium amount"
            )
        )
    )

    @Test
    fun invoke_delegatesToRepository() = runTest {
        coEvery { riskRepository.evaluateRisk(transaction) } returns expectedAssessment

        val result = useCase(transaction)

        assertEquals(expectedAssessment, result)
        coVerify(exactly = 1) { riskRepository.evaluateRisk(transaction) }
    }
}
