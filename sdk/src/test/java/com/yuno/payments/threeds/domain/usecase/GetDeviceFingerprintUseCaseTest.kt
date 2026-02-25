package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.DeviceFingerprint
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDeviceFingerprintUseCaseTest {

    private val fingerprintRepository: DeviceFingerprintRepository = mockk()
    private val useCase = GetDeviceFingerprintUseCase(fingerprintRepository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val expectedFingerprint = DeviceFingerprint(
            deviceId = "device-abc-123",
            successfulAuthCount = 5,
            lastAuthTimestamp = 999L
        )
        coEvery { fingerprintRepository.getFingerprint() } returns expectedFingerprint

        val result = useCase()

        assertEquals(expectedFingerprint, result)
        coVerify(exactly = 1) { fingerprintRepository.getFingerprint() }
    }
}
