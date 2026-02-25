package com.example.sebasiao.yuno.challenge.di

import android.app.Application
import com.example.sebasiao.yuno.challenge.data.repository.HardcodedSampleTransactionRepository
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionsUseCase
import com.yuno.payments.threeds.domain.model.AuthenticationResult

class AuthenticationResultHolder {
    private var _transactionId: String? = null
    private var _result: AuthenticationResult? = null

    fun set(transactionId: String, result: AuthenticationResult) {
        _transactionId = transactionId
        _result = result
    }

    fun get(): Pair<String, AuthenticationResult>? {
        val id = _transactionId ?: return null
        val result = _result ?: return null
        return id to result
    }

    fun clear() {
        _transactionId = null
        _result = null
    }
}

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

    val authResultHolder = AuthenticationResultHolder()
}
