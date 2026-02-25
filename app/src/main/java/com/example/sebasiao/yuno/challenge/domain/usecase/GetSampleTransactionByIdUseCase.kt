package com.example.sebasiao.yuno.challenge.domain.usecase

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.repository.SampleTransactionRepository

class GetSampleTransactionByIdUseCase(
    private val repository: SampleTransactionRepository
) {
    operator fun invoke(id: String): SampleTransaction? = repository.getById(id)
}
