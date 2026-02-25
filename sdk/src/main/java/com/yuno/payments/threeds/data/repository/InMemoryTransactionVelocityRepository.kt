package com.yuno.payments.threeds.data.repository

import com.yuno.payments.threeds.domain.model.TransactionVelocity
import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedDeque

internal class InMemoryTransactionVelocityRepository(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : TransactionVelocityRepository {

    private data class TransactionRecord(val transactionId: String, val timestamp: Long)

    private val records = ConcurrentLinkedDeque<TransactionRecord>()
    private val mutex = Mutex()

    override suspend fun recordTransaction(transactionId: String) {
        mutex.withLock {
            records.addLast(TransactionRecord(transactionId, timeProvider()))
        }
    }

    override suspend fun getVelocity(windowMillis: Long): TransactionVelocity {
        mutex.withLock {
            val cutoff = timeProvider() - windowMillis
            val count = records.count { it.timestamp >= cutoff }
            return TransactionVelocity(
                transactionsInWindow = count,
                windowSizeMillis = windowMillis
            )
        }
    }

    override suspend fun clearExpired(windowMillis: Long) {
        mutex.withLock {
            val cutoff = timeProvider() - windowMillis
            records.removeIf { it.timestamp < cutoff }
        }
    }
}
