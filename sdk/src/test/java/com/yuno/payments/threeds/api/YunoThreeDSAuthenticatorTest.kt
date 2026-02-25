package com.yuno.payments.threeds.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.yuno.payments.threeds.di.SdkContainer
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
import com.yuno.payments.threeds.presentation.challenge.ChallengeActivity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class YunoThreeDSAuthenticatorTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockAppContext: Context = mockk(relaxed = true)
    private val mockSharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)

    private val sampleDecision = AuthenticationDecision(
        riskAssessment = RiskAssessment(
            score = 30,
            riskLevel = RiskLevel.MEDIUM,
            factorResults = emptyList()
        ),
        action = AuthenticationAction.CHALLENGE,
        policyApplied = RiskPolicy.default()
    )

    @Before
    fun setUp() {
        SdkContainer.reset()
        every { mockContext.applicationContext } returns mockAppContext
        every {
            mockAppContext.getSharedPreferences(any(), any())
        } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit

        // Reset the risk policy back to default
        YunoThreeDSAuthenticator.updateRiskPolicy(RiskPolicy.default())
    }

    @Test
    fun initialize_withoutConfig_usesDefaults() {
        YunoThreeDSAuthenticator.initialize(mockContext)

        assertTrue(SdkContainer.isInitialized())
        assertEquals(RiskPolicy.default(), SdkContainer.config.riskPolicy)
        assertEquals(300_000L, SdkContainer.config.velocityWindowMillis)
        assertEquals("123456", SdkContainer.config.validOtp)
    }

    @Test
    fun initialize_calledTwice_isIdempotent() {
        val config1 = YunoThreeDSConfig.Builder()
            .validOtp("111111")
            .build()
        val config2 = YunoThreeDSConfig.Builder()
            .validOtp("222222")
            .build()

        YunoThreeDSAuthenticator.initialize(mockContext, config1)
        YunoThreeDSAuthenticator.initialize(mockContext, config2)

        // SdkContainer should keep the first config (idempotent)
        assertTrue(SdkContainer.isInitialized())
        assertEquals("111111", SdkContainer.config.validOtp)
    }

    @Test
    fun evaluateAndDecide_beforeInitialize_throwsClearError() = runTest {
        try {
            YunoThreeDSAuthenticator.evaluateAndDecide(
                com.yuno.payments.threeds.domain.model.Transaction(
                    id = "tx-1",
                    amount = 100.0,
                    currency = "USD",
                    merchantName = "Test",
                    cardLast4 = "1234",
                    customerTrustLevel = com.yuno.payments.threeds.domain.model.CustomerTrustLevel.NEW,
                    timestamp = System.currentTimeMillis()
                )
            )
            fail("Should have thrown IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("not been initialized"))
        }
    }

    @Test
    fun updateRiskPolicy_changesCurrentPolicy() {
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
                RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS,
                RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )

        YunoThreeDSAuthenticator.updateRiskPolicy(customPolicy)

        assertEquals(customPolicy, YunoThreeDSAuthenticator.getCurrentRiskPolicy())
    }

    @Test
    fun getCurrentRiskPolicy_returnsUpdatedPolicy() {
        val policy1 = RiskPolicy.default()
        YunoThreeDSAuthenticator.updateRiskPolicy(policy1)
        assertEquals(policy1, YunoThreeDSAuthenticator.getCurrentRiskPolicy())

        val policy2 = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.CHALLENGE,
                RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
                RiskLevel.HIGH to AuthenticationAction.BLOCK,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )
        YunoThreeDSAuthenticator.updateRiskPolicy(policy2)
        assertEquals(policy2, YunoThreeDSAuthenticator.getCurrentRiskPolicy())
    }

    @Test
    fun buildFrictionlessResult_returnsCorrectStatus() {
        val result = YunoThreeDSAuthenticator.buildFrictionlessResult(sampleDecision)

        assertEquals(AuthenticationStatus.AUTHENTICATED_FRICTIONLESS, result.status)
        assertEquals(sampleDecision, result.decision)
        assertNull(result.challengeCompletedAt)
        assertNull(result.abandonmentInfo)
    }

    @Test
    fun buildBlockedResult_returnsCorrectStatus() {
        val result = YunoThreeDSAuthenticator.buildBlockedResult(sampleDecision)

        assertEquals(AuthenticationStatus.BLOCKED, result.status)
        assertEquals(sampleDecision, result.decision)
        assertNull(result.challengeCompletedAt)
        assertNull(result.abandonmentInfo)
    }

    @Test
    fun parseChallengeResult_withSuccess_returnsAuthenticatedChallenge() {
        val intent = mockk<Intent>()
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_WAS_ABANDONED, false) } returns false
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_RESULT_SUCCESS, false) } returns true

        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = Activity.RESULT_OK,
            data = intent,
            decision = sampleDecision
        )

        assertEquals(AuthenticationStatus.AUTHENTICATED_CHALLENGE, result.status)
        assertEquals(sampleDecision, result.decision)
        assertNotNull(result.challengeCompletedAt)
        assertNull(result.abandonmentInfo)
    }

    @Test
    fun parseChallengeResult_withFailure_returnsChallengeFailedStatus() {
        val intent = mockk<Intent>()
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_WAS_ABANDONED, false) } returns false
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_RESULT_SUCCESS, false) } returns false

        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = Activity.RESULT_OK,
            data = intent,
            decision = sampleDecision
        )

        assertEquals(AuthenticationStatus.CHALLENGE_FAILED, result.status)
        assertEquals(sampleDecision, result.decision)
    }

    @Test
    fun parseChallengeResult_withCancelledResultCode_returnsChallengeFailedStatus() {
        val intent = mockk<Intent>()
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_WAS_ABANDONED, false) } returns false
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_RESULT_SUCCESS, false) } returns false

        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = Activity.RESULT_CANCELED,
            data = intent,
            decision = sampleDecision
        )

        assertEquals(AuthenticationStatus.CHALLENGE_FAILED, result.status)
    }

    @Test
    fun parseChallengeResult_withAbandonment_returnsAbandonedWithInfo() {
        val intent = mockk<Intent>()
        every { intent.getBooleanExtra(ChallengeActivity.EXTRA_WAS_ABANDONED, false) } returns true
        every {
            intent.getLongExtra(ChallengeActivity.EXTRA_ABANDONMENT_TIME_SPENT, 0L)
        } returns 15_000L
        every {
            intent.getIntExtra(ChallengeActivity.EXTRA_ABANDONMENT_OTP_ATTEMPTS, 0)
        } returns 2

        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = Activity.RESULT_CANCELED,
            data = intent,
            decision = sampleDecision
        )

        assertEquals(AuthenticationStatus.ABANDONED, result.status)
        assertEquals(sampleDecision, result.decision)
        assertNotNull(result.abandonmentInfo)
        assertEquals(15_000L, result.abandonmentInfo!!.timeSpentMillis)
        assertEquals(2, result.abandonmentInfo!!.otpAttemptsBeforeAbandon)
    }

    @Test
    fun parseChallengeResult_withNullIntent_returnsChallengeFailedStatus() {
        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = Activity.RESULT_CANCELED,
            data = null,
            decision = sampleDecision
        )

        assertEquals(AuthenticationStatus.CHALLENGE_FAILED, result.status)
    }

    @Test
    fun evaluateRisk_beforeInitialize_throwsClearError() = runTest {
        try {
            YunoThreeDSAuthenticator.evaluateRisk(
                com.yuno.payments.threeds.domain.model.Transaction(
                    id = "tx-1",
                    amount = 100.0,
                    currency = "USD",
                    merchantName = "Test",
                    cardLast4 = "1234",
                    customerTrustLevel = com.yuno.payments.threeds.domain.model.CustomerTrustLevel.NEW,
                    timestamp = System.currentTimeMillis()
                )
            )
            fail("Should have thrown IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("not been initialized"))
        }
    }

    @Test
    fun initialize_withCustomConfig_storesConfigInContainer() {
        val customConfig = YunoThreeDSConfig.Builder()
            .velocityWindowMillis(600_000L)
            .validOtp("987654")
            .build()

        YunoThreeDSAuthenticator.initialize(mockContext, customConfig)

        assertTrue(SdkContainer.isInitialized())
        assertEquals(600_000L, SdkContainer.config.velocityWindowMillis)
        assertEquals("987654", SdkContainer.config.validOtp)
    }

    @Test
    fun initialize_setsCurrentRiskPolicyFromConfig() {
        val customPolicy = RiskPolicy(
            actions = mapOf(
                RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
                RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS,
                RiskLevel.HIGH to AuthenticationAction.BLOCK,
                RiskLevel.CRITICAL to AuthenticationAction.BLOCK
            )
        )
        val config = YunoThreeDSConfig.Builder()
            .riskPolicy(customPolicy)
            .build()

        YunoThreeDSAuthenticator.initialize(mockContext, config)

        assertEquals(customPolicy, YunoThreeDSAuthenticator.getCurrentRiskPolicy())
    }
}
