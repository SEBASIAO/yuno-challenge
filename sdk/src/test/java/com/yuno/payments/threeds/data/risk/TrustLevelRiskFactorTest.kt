package com.yuno.payments.threeds.data.risk

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class TrustLevelRiskFactorTest {

    private lateinit var fingerprintRepository: DeviceFingerprintRepository
    private lateinit var sut: TrustLevelRiskFactor

    private val deviceTrustThreshold = 3

    @Before
    fun setUp() {
        fingerprintRepository = mockk()
        sut = TrustLevelRiskFactor(fingerprintRepository, deviceTrustThreshold)
    }

    private fun createTransaction(trustLevel: CustomerTrustLevel): Transaction = Transaction(
        id = "tx-1",
        amount = 100.0,
        currency = "USD",
        merchantName = "Test Merchant",
        cardLast4 = "1234",
        customerTrustLevel = trustLevel,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun evaluate_newCustomer_returnsScore70() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 0

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.NEW))

        assertEquals(70, result.score)
    }

    @Test
    fun evaluate_returningCustomer_returnsScore40() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 0

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.RETURNING))

        assertEquals(40, result.score)
    }

    @Test
    fun evaluate_trustedCustomer_returnsScore15() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 0

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.TRUSTED))

        assertEquals(15, result.score)
    }

    @Test
    fun evaluate_withDeviceBonus_reducesScore() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 5

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.RETURNING))

        // base 40 + bonus -15 = 25
        assertEquals(25, result.score)
        assertTrue(result.description.contains("device trust bonus"))
    }

    @Test
    fun evaluate_withDeviceBonus_clampedAtZero() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 10

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.TRUSTED))

        // base 15 + bonus -15 = 0, clamped to 0
        assertEquals(0, result.score)
    }

    @Test
    fun evaluate_returnsCorrectNameAndWeight() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 0

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.NEW))

        assertEquals("Customer Trust Level", result.name)
        assertEquals(0.35, result.weight, 0.001)
    }

    @Test
    fun evaluate_belowThreshold_noDeviceBonus() = runTest {
        coEvery { fingerprintRepository.getSuccessfulAuthCount() } returns 2

        val result = sut.evaluate(createTransaction(CustomerTrustLevel.NEW))

        assertEquals(70, result.score)
        assertTrue(!result.description.contains("device trust bonus"))
    }
}
