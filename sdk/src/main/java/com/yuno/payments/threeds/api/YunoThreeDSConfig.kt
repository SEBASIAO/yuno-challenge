package com.yuno.payments.threeds.api

import com.yuno.payments.threeds.domain.model.RiskPolicy

class YunoThreeDSConfig private constructor(
    val riskPolicy: RiskPolicy,
    val velocityWindowMillis: Long,
    val validOtp: String
) {
    class Builder {
        private var riskPolicy: RiskPolicy = RiskPolicy.default()
        private var velocityWindowMillis: Long = 300_000L
        private var validOtp: String = "123456"

        fun riskPolicy(policy: RiskPolicy) = apply { this.riskPolicy = policy }
        fun velocityWindowMillis(millis: Long) = apply { this.velocityWindowMillis = millis }
        fun validOtp(otp: String) = apply { this.validOtp = otp }

        fun build() = YunoThreeDSConfig(
            riskPolicy = riskPolicy,
            velocityWindowMillis = velocityWindowMillis,
            validOtp = validOtp
        )
    }
}
