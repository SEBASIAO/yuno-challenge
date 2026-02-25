package com.yuno.payments.threeds.domain.usecase

import com.yuno.payments.threeds.domain.model.DeviceFingerprint
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository

internal class GetDeviceFingerprintUseCase(
    private val fingerprintRepository: DeviceFingerprintRepository
) {
    suspend operator fun invoke(): DeviceFingerprint =
        fingerprintRepository.getFingerprint()
}
