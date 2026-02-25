package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sebasiao.yuno.challenge.di.AuthenticationResultHolder
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionsUseCase
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
import com.yuno.payments.threeds.domain.model.Transaction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionListViewModel(
    private val getSampleTransactions: GetSampleTransactionsUseCase,
    private val getSampleTransactionById: GetSampleTransactionByIdUseCase,
    private val resultHolder: AuthenticationResultHolder
) : ViewModel() {

    data class UiState(
        val transactions: List<SampleTransaction> = emptyList(),
        val activePolicy: PolicyOption = PolicyOption.POLICY_A,
        val isLoading: Boolean = true,
        val isProcessing: Boolean = false
    )

    enum class PolicyOption { POLICY_A, POLICY_B }

    sealed interface Event {
        data object LoadTransactions : Event
        data class TogglePolicy(val policy: PolicyOption) : Event
        data class TransactionClicked(val transactionId: String) : Event
    }

    sealed interface Effect {
        data class NavigateToResult(val transactionId: String) : Effect
        data class LaunchChallenge(
            val sdkTransaction: Transaction,
            val decision: AuthenticationDecision
        ) : Effect
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var pendingChallengeTransactionId: String? = null
    private var pendingChallengeDecision: AuthenticationDecision? = null

    init {
        loadTransactions()
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.LoadTransactions -> loadTransactions()
            is Event.TogglePolicy -> togglePolicy(event.policy)
            is Event.TransactionClicked -> processTransaction(event.transactionId)
        }
    }

    private fun loadTransactions() {
        val transactions = getSampleTransactions()
        _uiState.update { it.copy(transactions = transactions, isLoading = false) }
    }

    private fun togglePolicy(policy: PolicyOption) {
        _uiState.update { it.copy(activePolicy = policy) }
        val riskPolicy = when (policy) {
            PolicyOption.POLICY_A -> RiskPolicy.default()
            PolicyOption.POLICY_B -> RiskPolicy(
                mapOf(
                    RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
                    RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS,
                    RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
                    RiskLevel.CRITICAL to AuthenticationAction.BLOCK
                )
            )
        }
        YunoThreeDSAuthenticator.updateRiskPolicy(riskPolicy)
    }

    private fun processTransaction(transactionId: String) {
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val sample = getSampleTransactionById(transactionId)
            if (sample == null) {
                _uiState.update { it.copy(isProcessing = false) }
                return@launch
            }

            val sdkTransaction = sample.toSdkTransaction()
            val decision = YunoThreeDSAuthenticator.evaluateAndDecide(sdkTransaction)

            when (decision.action) {
                AuthenticationAction.FRICTIONLESS -> {
                    val result = YunoThreeDSAuthenticator.buildFrictionlessResult(decision)
                    resultHolder.set(transactionId, result)
                    _uiState.update { it.copy(isProcessing = false) }
                    _effects.send(Effect.NavigateToResult(transactionId))
                }
                AuthenticationAction.CHALLENGE -> {
                    pendingChallengeTransactionId = transactionId
                    pendingChallengeDecision = decision
                    _uiState.update { it.copy(isProcessing = false) }
                    _effects.send(Effect.LaunchChallenge(sdkTransaction, decision))
                }
                AuthenticationAction.BLOCK -> {
                    val result = YunoThreeDSAuthenticator.buildBlockedResult(decision)
                    resultHolder.set(transactionId, result)
                    _uiState.update { it.copy(isProcessing = false) }
                    _effects.send(Effect.NavigateToResult(transactionId))
                }
            }
        }
    }

    fun onChallengeResult(resultCode: Int, data: Intent?) {
        val transactionId = pendingChallengeTransactionId ?: return
        val decision = pendingChallengeDecision ?: return

        val result = YunoThreeDSAuthenticator.parseChallengeResult(resultCode, data, decision)
        resultHolder.set(transactionId, result)
        pendingChallengeTransactionId = null
        pendingChallengeDecision = null

        viewModelScope.launch {
            _effects.send(Effect.NavigateToResult(transactionId))
        }
    }

    private fun SampleTransaction.toSdkTransaction(): Transaction = Transaction(
        id = id,
        amount = amount,
        currency = currency,
        merchantName = merchantName,
        cardLast4 = cardLast4,
        customerTrustLevel = customerTrustLevel,
        timestamp = System.currentTimeMillis()
    )
}
