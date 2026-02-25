package com.yuno.payments.threeds.data.repository

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class InMemoryTransactionVelocityRepositoryTest {

    private lateinit var sut: InMemoryTransactionVelocityRepository

    @Before
    fun setUp() {
        sut = InMemoryTransactionVelocityRepository()
    }

    @Test
    fun recordAndGet_singleTransaction_returnsCount1() = runTest {
        sut.recordTransaction("tx-1")

        val velocity = sut.getVelocity(windowMillis = 60_000L)

        assertEquals(1, velocity.transactionsInWindow)
        assertEquals(60_000L, velocity.windowSizeMillis)
    }

    @Test
    fun recordAndGet_multipleTransactions_returnsCorrectCount() = runTest {
        sut.recordTransaction("tx-1")
        sut.recordTransaction("tx-2")
        sut.recordTransaction("tx-3")

        val velocity = sut.getVelocity(windowMillis = 60_000L)

        assertEquals(3, velocity.transactionsInWindow)
    }

    @Test
    fun getVelocity_expiredTransactions_notCounted() = runTest {
        var currentTime = 1000L
        val timedRepo = InMemoryTransactionVelocityRepository(
            timeProvider = { currentTime }
        )
        timedRepo.recordTransaction("tx-old")

        // Advance time past the window
        currentTime = 70_000L // 69 seconds later

        val velocity = timedRepo.getVelocity(windowMillis = 60_000L)

        // tx-old was recorded at 1000, cutoff = 70000 - 60000 = 10000
        // 1000 < 10000, so it's expired
        assertEquals(0, velocity.transactionsInWindow)
    }

    @Test
    fun clearExpired_removesOldRecords() = runTest {
        var currentTime = 1000L
        val timedRepo = InMemoryTransactionVelocityRepository(
            timeProvider = { currentTime }
        )
        timedRepo.recordTransaction("tx-old")

        // Record a new transaction at a later time
        currentTime = 50_000L
        timedRepo.recordTransaction("tx-new")

        // Clear with a window that expires the old one
        currentTime = 70_000L
        timedRepo.clearExpired(windowMillis = 60_000L)

        val velocity = timedRepo.getVelocity(windowMillis = 300_000L)

        // Only tx-new should remain (recorded at 50000, cutoff for clear = 70000-60000=10000)
        // tx-old at 1000 < 10000 -> cleared
        // tx-new at 50000 >= 10000 -> kept
        assertEquals(1, velocity.transactionsInWindow)
    }

    @Test
    fun concurrentWrites_noDataLoss() = runTest {
        val count = 100

        val jobs = (1..count).map { i ->
            launch {
                sut.recordTransaction("tx-$i")
            }
        }
        jobs.forEach { it.join() }

        val velocity = sut.getVelocity(windowMillis = 60_000L)

        assertEquals(count, velocity.transactionsInWindow)
    }

    @Test
    fun getVelocity_noTransactions_returnsZero() = runTest {
        val velocity = sut.getVelocity(windowMillis = 60_000L)

        assertEquals(0, velocity.transactionsInWindow)
    }
}
