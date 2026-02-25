package com.example.sebasiao.yuno.challenge.data.repository

import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HardcodedSampleTransactionRepositoryTest {

    private lateinit var repository: HardcodedSampleTransactionRepository

    @Before
    fun setUp() {
        repository = HardcodedSampleTransactionRepository()
    }

    @Test
    fun getSampleTransactions_returns18Transactions() {
        val transactions = repository.getSampleTransactions()

        assertEquals(18, transactions.size)
    }

    @Test
    fun getSampleTransactions_coversAllScenarios() {
        val transactions = repository.getSampleTransactions()
        val scenariosPresent = transactions.map { it.scenario }.toSet()

        TransactionScenario.entries.filter { it != TransactionScenario.CUSTOM }.forEach { scenario ->
            assertTrue(
                "Scenario $scenario should be present in sample transactions",
                scenariosPresent.contains(scenario)
            )
        }
    }

    @Test
    fun getSampleTransactions_hasUniqueIds() {
        val transactions = repository.getSampleTransactions()
        val ids = transactions.map { it.id }

        assertEquals("All transaction IDs must be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun getSampleTransactions_frictionlessScenarioHas3Transactions() {
        val transactions = repository.getSampleTransactions()
        val frictionless = transactions.filter { it.scenario == TransactionScenario.FRICTIONLESS_LOW_RISK }

        assertEquals(3, frictionless.size)
    }

    @Test
    fun getSampleTransactions_challengeMediumRiskHas6Transactions() {
        val transactions = repository.getSampleTransactions()
        val challenge = transactions.filter { it.scenario == TransactionScenario.CHALLENGE_MEDIUM_RISK }

        assertEquals(6, challenge.size)
    }

    @Test
    fun getSampleTransactions_blockedHighRiskHas3Transactions() {
        val transactions = repository.getSampleTransactions()
        val blocked = transactions.filter { it.scenario == TransactionScenario.BLOCKED_HIGH_RISK }

        assertEquals(3, blocked.size)
    }

    @Test
    fun getSampleTransactions_velocityTriggerHas3Transactions() {
        val transactions = repository.getSampleTransactions()
        val velocity = transactions.filter { it.scenario == TransactionScenario.VELOCITY_TRIGGER }

        assertEquals(3, velocity.size)
    }

    @Test
    fun getSampleTransactions_trustedDeviceHas1Transaction() {
        val transactions = repository.getSampleTransactions()
        val trusted = transactions.filter { it.scenario == TransactionScenario.TRUSTED_DEVICE }

        assertEquals(1, trusted.size)
    }

    @Test
    fun getSampleTransactions_newCustomerHas1Transaction() {
        val transactions = repository.getSampleTransactions()
        val newCustomer = transactions.filter { it.scenario == TransactionScenario.NEW_CUSTOMER }

        assertEquals(1, newCustomer.size)
    }

    @Test
    fun getSampleTransactions_abandonmentTestHas1Transaction() {
        val transactions = repository.getSampleTransactions()
        val abandonment = transactions.filter { it.scenario == TransactionScenario.ABANDONMENT_TEST }

        assertEquals(1, abandonment.size)
    }

    @Test
    fun getById_withValidId_returnsTransaction() {
        val transactions = repository.getSampleTransactions()
        val firstTransaction = transactions.first()

        val result = repository.getById(firstTransaction.id)

        assertNotNull(result)
        assertEquals(firstTransaction, result)
    }

    @Test
    fun getById_withInvalidId_returnsNull() {
        val result = repository.getById("non-existent-id")

        assertNull(result)
    }

    @Test
    fun getById_returnsCorrectTransactionForEachId() {
        val transactions = repository.getSampleTransactions()

        transactions.forEach { transaction ->
            val result = repository.getById(transaction.id)
            assertNotNull("Transaction with id ${transaction.id} should be found", result)
            assertEquals(transaction, result)
        }
    }

    @Test
    fun getSampleTransactions_allTransactionsHaveValidData() {
        val transactions = repository.getSampleTransactions()

        transactions.forEach { transaction ->
            assertTrue(
                "Transaction ${transaction.id} should have non-empty id",
                transaction.id.isNotBlank()
            )
            assertTrue(
                "Transaction ${transaction.id} should have positive amount",
                transaction.amount > 0.0
            )
            assertTrue(
                "Transaction ${transaction.id} should have non-empty currency",
                transaction.currency.isNotBlank()
            )
            assertTrue(
                "Transaction ${transaction.id} should have non-empty merchant name",
                transaction.merchantName.isNotBlank()
            )
            assertTrue(
                "Transaction ${transaction.id} should have non-empty cardLast4",
                transaction.cardLast4.isNotBlank()
            )
            assertTrue(
                "Transaction ${transaction.id} should have non-empty scenario description",
                transaction.scenarioDescription.isNotBlank()
            )
        }
    }
}
