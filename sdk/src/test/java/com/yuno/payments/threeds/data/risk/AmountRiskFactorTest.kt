package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class AmountRiskFactorTest {

    private lateinit var sut: AmountRiskFactor

    @Before
    fun setUp() {
        sut = AmountRiskFactor()
    }

    private fun createTransaction(amount: Double): Transaction = Transaction(
        id = "tx-1",
        amount = amount,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.NEW,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun evaluate_amountUnder50_returnsScore10() = runTest {
        val transaction = createTransaction(amount = 25.0)

        val result = sut.evaluate(transaction)

        assertEquals(10, result.score)
        assertEquals("Low amount transaction", result.description)
    }

    @Test
    fun evaluate_amount100_returnsScore50() = runTest {
        val transaction = createTransaction(amount = 100.0)

        val result = sut.evaluate(transaction)

        assertEquals(50, result.score)
        assertEquals("Moderate amount transaction", result.description)
    }

    @Test
    fun evaluate_amount300_returnsScore75() = runTest {
        val transaction = createTransaction(amount = 300.0)

        val result = sut.evaluate(transaction)

        assertEquals(75, result.score)
        assertEquals("High amount transaction", result.description)
    }

    @Test
    fun evaluate_amount500_returnsScore95() = runTest {
        val transaction = createTransaction(amount = 500.0)

        val result = sut.evaluate(transaction)

        assertEquals(95, result.score)
        assertEquals("Very high amount transaction", result.description)
    }

    @Test
    fun evaluate_returnsCorrectWeightAndName() = runTest {
        val transaction = createTransaction(amount = 10.0)

        val result = sut.evaluate(transaction)

        assertEquals("Transaction Amount", result.name)
        assertEquals(0.35, result.weight, 0.001)
    }
}
