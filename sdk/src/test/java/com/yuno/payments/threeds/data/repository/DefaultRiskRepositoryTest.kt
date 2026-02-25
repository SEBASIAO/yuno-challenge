package com.yuno.payments.threeds.data.repository

import com.yuno.payments.threeds.data.risk.RiskScoreEngine
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class DefaultRiskRepositoryTest {

    private lateinit var engine: RiskScoreEngine
    private lateinit var sut: DefaultRiskRepository

    @Before
    fun setUp() {
        engine = mockk()
        sut = DefaultRiskRepository(engine)
    }

    @Test
    fun evaluateRisk_delegatesToEngine() = runTest {
        val transaction = Transaction(
            id = "tx-1",
            amount = 100.0,
            currency = "USD",
            merchantName = "Test Merchant",
            cardLast4 = "1234",
            customerTrustLevel = CustomerTrustLevel.NEW,
            timestamp = System.currentTimeMillis()
        )
        val expectedAssessment = RiskAssessment(
            score = 50,
            riskLevel = RiskLevel.MEDIUM,
            factorResults = emptyList()
        )
        coEvery { engine.evaluate(transaction) } returns expectedAssessment

        val result = sut.evaluateRisk(transaction)

        assertEquals(expectedAssessment, result)
        coVerify(exactly = 1) { engine.evaluate(transaction) }
    }
}
