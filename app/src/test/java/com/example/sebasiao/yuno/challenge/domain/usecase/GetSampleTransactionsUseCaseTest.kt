package com.example.sebasiao.yuno.challenge.domain.usecase

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.domain.repository.SampleTransactionRepository
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSampleTransactionsUseCaseTest {

    private lateinit var repository: SampleTransactionRepository
    private lateinit var useCase: GetSampleTransactionsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSampleTransactionsUseCase(repository)
    }

    @Test
    fun invoke_delegatesToRepository() {
        val expectedTransactions = listOf(
            SampleTransaction(
                id = "test-1",
                amount = 10.0,
                currency = "USD",
                merchantName = "Test Store",
                cardLast4 = "1234",
                customerTrustLevel = CustomerTrustLevel.TRUSTED,
                scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
                scenarioDescription = "Test scenario"
            )
        )
        every { repository.getSampleTransactions() } returns expectedTransactions

        val result = useCase()

        assertEquals(expectedTransactions, result)
        verify(exactly = 1) { repository.getSampleTransactions() }
    }

    @Test
    fun invoke_returnsEmptyListWhenRepositoryReturnsEmpty() {
        every { repository.getSampleTransactions() } returns emptyList()

        val result = useCase()

        assertEquals(emptyList<SampleTransaction>(), result)
        verify(exactly = 1) { repository.getSampleTransactions() }
    }
}
