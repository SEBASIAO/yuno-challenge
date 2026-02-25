package com.example.sebasiao.yuno.challenge.domain.usecase

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.repository.SampleTransactionRepository

class GetSampleTransactionsUseCase(
    private val repository: SampleTransactionRepository
) {
    operator fun invoke(): List<SampleTransaction> = repository.getSampleTransactions()
}
