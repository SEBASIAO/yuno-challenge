package com.yuno.payments.threeds.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.yuno.payments.threeds.di.SdkContainer
import com.yuno.payments.threeds.domain.model.AbandonmentInfo
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.DeviceFingerprint
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskPolicy
import com.yuno.payments.threeds.domain.model.Transaction
import com.yuno.payments.threeds.presentation.challenge.ChallengeActivity

object YunoThreeDSAuthenticator {

    private var currentRiskPolicy: RiskPolicy = RiskPolicy.default()

    /**
     * Initialize with default config. Plug & play.
     */
    fun initialize(context: Context) {
        initialize(context, YunoThreeDSConfig.Builder().build())
    }

    /**
     * Initialize with custom config.
     */
    fun initialize(context: Context, config: YunoThreeDSConfig) {
        currentRiskPolicy = config.riskPolicy
        SdkContainer.initialize(context, config)
    }

    /**
     * Evaluate risk only. Returns raw assessment without applying policy.
     */
    suspend fun evaluateRisk(transaction: Transaction): RiskAssessment {
        checkInitialized()
        return SdkContainer.evaluateTransactionRisk(transaction)
    }

    /**
     * Evaluate risk and apply the current policy.
     * Returns the decision (assessment + action + policy used).
     */
    suspend fun evaluateAndDecide(transaction: Transaction): AuthenticationDecision {
        checkInitialized()
        return SdkContainer.authenticateTransaction(transaction, currentRiskPolicy)
    }

    /**
     * Launch the challenge UI. Requires a prior [decision] from [evaluateAndDecide]
     * to ensure the caller has verified that a challenge is needed.
     */
    @Suppress("UNUSED_PARAMETER")
    fun launchChallenge(
        transaction: Transaction,
        decision: AuthenticationDecision,
        launcher: ActivityResultLauncher<Intent>,
        context: Context
    ) {
        checkInitialized()
        val config = SdkContainer.config
        val intent = ChallengeActivity.createIntent(
            context = context,
            merchantName = transaction.merchantName,
            amount = "${transaction.currency} ${transaction.amount}",
            cardLast4 = transaction.cardLast4,
            validOtp = config.validOtp
        )
        launcher.launch(intent)
    }

    /**
     * Parse the result from ChallengeActivity.
     */
    fun parseChallengeResult(
        resultCode: Int,
        data: Intent?,
        decision: AuthenticationDecision
    ): AuthenticationResult {
        val wasAbandoned = data?.getBooleanExtra(
            ChallengeActivity.EXTRA_WAS_ABANDONED, false
        ) ?: false

        if (wasAbandoned) {
            val timeSpent = data?.getLongExtra(
                ChallengeActivity.EXTRA_ABANDONMENT_TIME_SPENT, 0L
            ) ?: 0L
            val otpAttempts = data?.getIntExtra(
                ChallengeActivity.EXTRA_ABANDONMENT_OTP_ATTEMPTS, 0
            ) ?: 0
            return AuthenticationResult(
                status = AuthenticationStatus.ABANDONED,
                decision = decision,
                abandonmentInfo = AbandonmentInfo(
                    abandonedAt = System.currentTimeMillis(),
                    timeSpentMillis = timeSpent,
                    otpAttemptsBeforeAbandon = otpAttempts
                )
            )
        }

        val isSuccess = data?.getBooleanExtra(
            ChallengeActivity.EXTRA_RESULT_SUCCESS, false
        ) ?: false
        return if (resultCode == Activity.RESULT_OK && isSuccess) {
            AuthenticationResult(
                status = AuthenticationStatus.AUTHENTICATED_CHALLENGE,
                decision = decision,
                challengeCompletedAt = System.currentTimeMillis()
            )
        } else {
            AuthenticationResult(
                status = AuthenticationStatus.CHALLENGE_FAILED,
                decision = decision
            )
        }
    }

    /**
     * Build a frictionless result (no challenge needed).
     */
    fun buildFrictionlessResult(decision: AuthenticationDecision): AuthenticationResult {
        return AuthenticationResult(
            status = AuthenticationStatus.AUTHENTICATED_FRICTIONLESS,
            decision = decision
        )
    }

    /**
     * Build a blocked result.
     */
    fun buildBlockedResult(decision: AuthenticationDecision): AuthenticationResult {
        return AuthenticationResult(
            status = AuthenticationStatus.BLOCKED,
            decision = decision
        )
    }

    /**
     * Update risk policy at runtime for A/B testing.
     */
    fun updateRiskPolicy(policy: RiskPolicy) {
        currentRiskPolicy = policy
    }

    /**
     * Get current risk policy.
     */
    fun getCurrentRiskPolicy(): RiskPolicy = currentRiskPolicy

    /**
     * Update device trust data after a successful authentication.
     */
    suspend fun updateDeviceTrust() {
        checkInitialized()
        SdkContainer.updateDeviceTrust()
    }

    /**
     * Get device fingerprint info.
     */
    internal suspend fun getDeviceInfo(): DeviceFingerprint {
        checkInitialized()
        return SdkContainer.getDeviceFingerprint()
    }

    private fun checkInitialized() {
        check(SdkContainer.isInitialized()) {
            "YunoThreeDSAuthenticator has not been initialized. Call initialize(context) first."
        }
    }
}
