package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository

internal class RecordTransactionVelocityUseCase(
    private val velocityRepository: TransactionVelocityRepository
) {
    suspend operator fun invoke(transactionId: String) {
        velocityRepository.recordTransaction(transactionId)
    }
}
