package com.example.sebasiao.yuno.challenge.domain.usecase

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.domain.repository.SampleTransactionRepository
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetSampleTransactionByIdUseCaseTest {

    private lateinit var repository: SampleTransactionRepository
    private lateinit var useCase: GetSampleTransactionByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSampleTransactionByIdUseCase(repository)
    }

    @Test
    fun invoke_delegatesToRepository() {
        val expectedTransaction = SampleTransaction(
            id = "test-1",
            amount = 10.0,
            currency = "USD",
            merchantName = "Test Store",
            cardLast4 = "1234",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
            scenarioDescription = "Test scenario"
        )
        every { repository.getById("test-1") } returns expectedTransaction

        val result = useCase("test-1")

        assertEquals(expectedTransaction, result)
        verify(exactly = 1) { repository.getById("test-1") }
    }

    @Test
    fun invoke_returnsNullWhenTransactionNotFound() {
        every { repository.getById("non-existent") } returns null

        val result = useCase("non-existent")

        assertNull(result)
        verify(exactly = 1) { repository.getById("non-existent") }
    }
}
