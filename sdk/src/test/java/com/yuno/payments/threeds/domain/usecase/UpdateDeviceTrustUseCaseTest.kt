package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateDeviceTrustUseCaseTest {

    private val fingerprintRepository: DeviceFingerprintRepository = mockk()
    private val useCase = UpdateDeviceTrustUseCase(fingerprintRepository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        coEvery { fingerprintRepository.recordSuccessfulAuth() } returns Unit

        useCase()

        coVerify(exactly = 1) { fingerprintRepository.recordSuccessfulAuth() }
    }
}
