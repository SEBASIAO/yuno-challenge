package com.yuno.payments.threeds.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskPolicyTest {

    @Test
    fun actionFor_withDefaultPolicy_returnsFrictionlessForLow() {
        val policy = RiskPolicy.default()

        val action = policy.actionFor(RiskLevel.LOW)

        assertEquals(AuthenticationAction.FRICTIONLESS, action)
    }

    @Test
    fun actionFor_withDefaultPolicy_returnsChallengeForMedium() {
        val policy = RiskPolicy.default()

        val action = policy.actionFor(RiskLevel.MEDIUM)

        assertEquals(AuthenticationAction.CHALLENGE, action)
    }

    @Test
    fun actionFor_withDefaultPolicy_returnsChallengeForHigh() {
        val policy = RiskPolicy.default()

        val action = policy.actionFor(RiskLevel.HIGH)

        assertEquals(AuthenticationAction.CHALLENGE, action)
    }

    @Test
    fun actionFor_withDefaultPolicy_returnsBlockForCritical() {
        val policy = RiskPolicy.default()

        val action = policy.actionFor(RiskLevel.CRITICAL)

        assertEquals(AuthenticationAction.BLOCK, action)
    }

    @Test
    fun actionFor_withCustomPolicy_returnsOverriddenAction() {
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.CHALLENGE,
                RiskLevel.MEDIUM to AuthenticationAction.BLOCK,
                RiskLevel.HIGH to AuthenticationAction.BLOCK,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )

        assertEquals(AuthenticationAction.CHALLENGE, customPolicy.actionFor(RiskLevel.LOW))
        assertEquals(AuthenticationAction.BLOCK, customPolicy.actionFor(RiskLevel.MEDIUM))
    }

    @Test
    fun actionFor_withPartialPolicy_usesDefaultFallback() {
        val partialPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.BLOCK
            )
        )

        assertEquals(AuthenticationAction.BLOCK, partialPolicy.actionFor(RiskLevel.LOW))
        assertEquals(AuthenticationAction.CHALLENGE, partialPolicy.actionFor(RiskLevel.MEDIUM))
        assertEquals(AuthenticationAction.CHALLENGE, partialPolicy.actionFor(RiskLevel.HIGH))
        assertEquals(AuthenticationAction.BLOCK, partialPolicy.actionFor(RiskLevel.CRITICAL))
    }

    @Test
    fun default_containsAllRiskLevels() {
        val defaultPolicy = RiskPolicy.default()

        RiskLevel.entries.forEach { level ->
            val action = defaultPolicy.actionFor(level)
            assertTrue(
                "Default policy should have an action for $level",
                action in AuthenticationAction.entries
            )
        }
    }
}
