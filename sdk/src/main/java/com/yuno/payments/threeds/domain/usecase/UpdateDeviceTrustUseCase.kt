package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository

internal class UpdateDeviceTrustUseCase(
    private val fingerprintRepository: DeviceFingerprintRepository
) {
    suspend operator fun invoke() {
        fingerprintRepository.recordSuccessfulAuth()
    }
}
