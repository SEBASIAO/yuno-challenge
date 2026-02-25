package com.yuno.payments.threeds.domain.repository

import com.yuno.payments.threeds.domain.model.TransactionVelocity

internal interface TransactionVelocityRepository {
    suspend fun recordTransaction(transactionId: String)
    suspend fun getVelocity(windowMillis: Long): TransactionVelocity
    suspend fun clearExpired(windowMillis: Long)
}
