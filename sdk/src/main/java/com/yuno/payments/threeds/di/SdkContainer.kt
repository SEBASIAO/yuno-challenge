package com.yuno.payments.threeds.di

import android.content.Context
import android.content.SharedPreferences
import com.yuno.payments.threeds.api.YunoThreeDSConfig
import com.yuno.payments.threeds.data.repository.DefaultRiskRepository
import com.yuno.payments.threeds.data.repository.InMemoryTransactionVelocityRepository
import com.yuno.payments.threeds.data.repository.SharedPrefsDeviceFingerprintRepository
import com.yuno.payments.threeds.data.risk.AmountRiskFactor
import com.yuno.payments.threeds.data.risk.RiskFactor
import com.yuno.payments.threeds.data.risk.RiskScoreEngine
import com.yuno.payments.threeds.data.risk.TrustLevelRiskFactor
import com.yuno.payments.threeds.data.risk.VelocityRiskFactor
import com.yuno.payments.threeds.domain.repository.DeviceFingerprintRepository
import com.yuno.payments.threeds.domain.repository.RiskRepository
import com.yuno.payments.threeds.domain.repository.TransactionVelocityRepository
import com.yuno.payments.threeds.domain.usecase.AuthenticateTransactionUseCase
import com.yuno.payments.threeds.domain.usecase.EvaluateTransactionRiskUseCase
import com.yuno.payments.threeds.domain.usecase.GetDeviceFingerprintUseCase
import com.yuno.payments.threeds.domain.usecase.RecordTransactionVelocityUseCase
import com.yuno.payments.threeds.domain.usecase.ResolveAuthenticationActionUseCase
import com.yuno.payments.threeds.domain.usecase.TrackChallengeAbandonmentUseCase
import com.yuno.payments.threeds.domain.usecase.UpdateDeviceTrustUseCase

internal object SdkContainer {

    @Volatile
    private var initialized = false

    lateinit var config: YunoThreeDSConfig
        private set

    private lateinit var appContext: Context

    // Lazy singletons
    private val sharedPreferences: SharedPreferences by lazy {
        appContext.getSharedPreferences("yuno_threeds_prefs", Context.MODE_PRIVATE)
    }

    val velocityRepository: TransactionVelocityRepository by lazy {
        InMemoryTransactionVelocityRepository()
    }

    val fingerprintRepository: DeviceFingerprintRepository by lazy {
        SharedPrefsDeviceFingerprintRepository(sharedPreferences)
    }

    private val riskFactors: List<RiskFactor> by lazy {
        listOf(
            AmountRiskFactor(),
            VelocityRiskFactor(velocityRepository, config.velocityWindowMillis),
            TrustLevelRiskFactor(fingerprintRepository)
        )
    }

    private val riskEngine: RiskScoreEngine by lazy { RiskScoreEngine(riskFactors) }

    val riskRepository: RiskRepository by lazy { DefaultRiskRepository(riskEngine) }

    // Use cases
    val evaluateTransactionRisk by lazy { EvaluateTransactionRiskUseCase(riskRepository) }
    val resolveAuthenticationAction by lazy { ResolveAuthenticationActionUseCase() }
    val recordTransactionVelocity by lazy { RecordTransactionVelocityUseCase(velocityRepository) }
    val authenticateTransaction by lazy {
        AuthenticateTransactionUseCase(
            evaluateTransactionRisk,
            resolveAuthenticationAction,
            recordTransactionVelocity
        )
    }
    val getDeviceFingerprint by lazy { GetDeviceFingerprintUseCase(fingerprintRepository) }
    val updateDeviceTrust by lazy { UpdateDeviceTrustUseCase(fingerprintRepository) }
    val trackChallengeAbandonment by lazy { TrackChallengeAbandonmentUseCase() }

    @Synchronized
    fun initialize(context: Context, config: YunoThreeDSConfig) {
        if (initialized) return
        this.appContext = context.applicationContext
        this.config = config
        initialized = true
    }

    fun isInitialized(): Boolean = initialized

    // For testing only
    @Synchronized
    internal fun reset() {
        initialized = false
    }
}
