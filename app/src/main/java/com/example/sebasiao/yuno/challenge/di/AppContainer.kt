package com.example.sebasiao.yuno.challenge.di

import android.app.Application
import com.example.sebasiao.yuno.challenge.data.repository.HardcodedSampleTransactionRepository
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionsUseCase

class AppContainer(
    @Suppress("UNUSED_PARAMETER") application: Application
) {
    private val sampleTransactionRepo by lazy { HardcodedSampleTransactionRepository() }

    val getSampleTransactions by lazy {
        GetSampleTransactionsUseCase(sampleTransactionRepo)
    }

    val getSampleTransactionById by lazy {
        GetSampleTransactionByIdUseCase(sampleTransactionRepo)
    }
}
