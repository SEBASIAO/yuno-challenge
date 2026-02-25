package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordTransactionVelocityUseCaseTest {

    private val velocityRepository: TransactionVelocityRepository = mockk()
    private val useCase = RecordTransactionVelocityUseCase(velocityRepository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val transactionId = "txn-001"
        coEvery { velocityRepository.recordTransaction(transactionId) } returns Unit

        useCase(transactionId)

        coVerify(exactly = 1) { velocityRepository.recordTransaction(transactionId) }
    }
}
