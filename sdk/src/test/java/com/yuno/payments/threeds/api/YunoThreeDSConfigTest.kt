package com.yuno.payments.threeds.api

import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class YunoThreeDSConfigTest {

    @Test
    fun builder_withNoCustomization_usesAllDefaults() {
        val config = YunoThreeDSConfig.Builder().build()

        assertEquals(RiskPolicy.default(), config.riskPolicy)
        assertEquals(300_000L, config.velocityWindowMillis)
        assertEquals("123456", config.validOtp)
    }

    @Test
    fun builder_withCustomPolicy_overridesDefault() {
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
                RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS,
                RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )

        val config = YunoThreeDSConfig.Builder()
            .riskPolicy(customPolicy)
            .build()

        assertEquals(customPolicy, config.riskPolicy)
        assertEquals(AuthenticationAction.FRICTIONLESS, config.riskPolicy.actionFor(RiskLevel.MEDIUM))
    }

    @Test
    fun builder_withCustomVelocityWindow_overridesDefault() {
        val config = YunoThreeDSConfig.Builder()
            .velocityWindowMillis(600_000L)
            .build()

        assertEquals(600_000L, config.velocityWindowMillis)
    }

    @Test
    fun builder_withCustomOtp_overridesDefault() {
        val config = YunoThreeDSConfig.Builder()
            .validOtp("999888")
            .build()

        assertEquals("999888", config.validOtp)
    }

    @Test
    fun builder_build_isImmutable() {
        val builder = YunoThreeDSConfig.Builder()
            .velocityWindowMillis(100_000L)
            .validOtp("111111")

        val config1 = builder.build()
        val config2 = builder.velocityWindowMillis(200_000L).build()

        assertNotSame(config1, config2)
        assertEquals(100_000L, config1.velocityWindowMillis)
        assertEquals(200_000L, config2.velocityWindowMillis)
        assertEquals("111111", config1.validOtp)
        assertEquals("111111", config2.validOtp)
    }

    @Test
    fun builder_chainingAllSetters_producesCorrectConfig() {
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
                RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
                RiskLevel.HIGH to AuthenticationAction.BLOCK,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )

        val config = YunoThreeDSConfig.Builder()
            .riskPolicy(customPolicy)
            .velocityWindowMillis(120_000L)
            .validOtp("654321")
            .build()

        assertEquals(customPolicy, config.riskPolicy)
        assertEquals(120_000L, config.velocityWindowMillis)
        assertEquals("654321", config.validOtp)
    }
}
