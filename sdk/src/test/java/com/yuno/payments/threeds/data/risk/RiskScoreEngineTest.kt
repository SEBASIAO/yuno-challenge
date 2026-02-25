package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RiskScoreEngineTest {

    private fun createTransaction(): Transaction = Transaction(
        id = "tx-1",
        amount = 100.0,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.NEW,
        timestamp = System.currentTimeMillis()
    )

    private fun createFakeFactor(score: Int, weight: Double = 1.0): RiskFactor {
        val factor = mockk<RiskFactor>()
        coEvery { factor.name } returns "Fake Factor"
        coEvery { factor.weight } returns weight
        coEvery { factor.evaluate(any()) } returns RiskFactorResult(
            name = "Fake Factor",
            score = score,
            weight = weight,
            description = "Fake description"
        )
        return factor
    }

    @Test
    fun evaluate_withLowScores_returnsLowRiskLevel() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 10)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(10, result.score)
    }

    @Test
    fun evaluate_withMediumScores_returnsMediumRiskLevel() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 40)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(40, result.score)
    }

    @Test
    fun evaluate_withHighScores_returnsHighRiskLevel() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 60)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertEquals(60, result.score)
    }

    @Test
    fun evaluate_withCriticalScores_returnsCriticalRiskLevel() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 90)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.CRITICAL, result.riskLevel)
        assertEquals(90, result.score)
    }

    @Test
    fun evaluate_scoreAtBoundary25_returnsLow() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 25)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(25, result.score)
    }

    @Test
    fun evaluate_scoreAtBoundary26_returnsMedium() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 26)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(26, result.score)
    }

    @Test
    fun evaluate_scoreAtBoundary50_returnsMedium() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 50)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(50, result.score)
    }

    @Test
    fun evaluate_scoreAtBoundary51_returnsHigh() = runTest {
        val engine = RiskScoreEngine(listOf(createFakeFactor(score = 51)))

        val result = engine.evaluate(createTransaction())

        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertEquals(51, result.score)
    }

    @Test
    fun evaluate_combinesFactorsProperly() = runTest {
        // Factor A: score=20, weight=0.5  -> weighted contribution = 10
        // Factor B: score=80, weight=0.5  -> weighted contribution = 40
        // Total weighted = 50, total weight = 1.0, normalized = 50
        val factorA = createFakeFactor(score = 20, weight = 0.5)
        val factorB = createFakeFactor(score = 80, weight = 0.5)
        val engine = RiskScoreEngine(listOf(factorA, factorB))

        val result = engine.evaluate(createTransaction())

        assertEquals(50, result.score)
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertEquals(2, result.factorResults.size)
    }

    @Test
    fun evaluate_emptyFactors_returnsZeroScore() = runTest {
        val engine = RiskScoreEngine(emptyList())

        val result = engine.evaluate(createTransaction())

        assertEquals(0, result.score)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(0, result.factorResults.size)
    }

    @Test
    fun evaluate_unequalWeights_calculatesWeightedAverage() = runTest {
        // Factor A: score=100, weight=0.75 -> weighted contribution = 75
        // Factor B: score=0, weight=0.25   -> weighted contribution = 0
        // Total weighted = 75, total weight = 1.0, normalized = 75
        val factorA = createFakeFactor(score = 100, weight = 0.75)
        val factorB = createFakeFactor(score = 0, weight = 0.25)
        val engine = RiskScoreEngine(listOf(factorA, factorB))

        val result = engine.evaluate(createTransaction())

        assertEquals(75, result.score)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }
}
