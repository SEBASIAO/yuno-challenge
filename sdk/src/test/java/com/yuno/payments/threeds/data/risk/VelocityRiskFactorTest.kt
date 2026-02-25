package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.model.TransactionVelocity
import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class VelocityRiskFactorTest {

    private lateinit var velocityRepository: TransactionVelocityRepository
    private lateinit var sut: VelocityRiskFactor

    private val windowMillis = 300_000L

    @Before
    fun setUp() {
        velocityRepository = mockk()
        sut = VelocityRiskFactor(velocityRepository, windowMillis)
    }

    private fun createTransaction(): Transaction = Transaction(
        id = "tx-1",
        amount = 100.0,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.NEW,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun evaluate_zeroTransactions_returnsScore5() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 0,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals(5, result.score)
    }

    @Test
    fun evaluate_oneTransaction_returnsScore5() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 1,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals(5, result.score)
    }

    @Test
    fun evaluate_twoTransactions_returnsScore40() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 2,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals(40, result.score)
    }

    @Test
    fun evaluate_threeTransactions_returnsScore75() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 3,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals(75, result.score)
    }

    @Test
    fun evaluate_fourPlusTransactions_returnsScore100() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 5,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals(100, result.score)
    }

    @Test
    fun evaluate_returnsCorrectNameAndWeight() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 1,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals("Transaction Velocity", result.name)
        assertEquals(0.30, result.weight, 0.001)
    }

    @Test
    fun evaluate_descriptionContainsTransactionCount() = runTest {
        coEvery { velocityRepository.getVelocity(windowMillis) } returns TransactionVelocity(
            transactionsInWindow = 3,
            windowSizeMillis = windowMillis
        )

        val result = sut.evaluate(createTransaction())

        assertEquals("3 transactions in last 5 minutes", result.description)
    }
}
