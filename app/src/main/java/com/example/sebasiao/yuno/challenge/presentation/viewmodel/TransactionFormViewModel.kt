package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sebasiao.yuno.challenge.di.AuthenticationResultHolder
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionFormViewModel(
    private val resultHolder: AuthenticationResultHolder
) : ViewModel() {

    data class UiState(
        val amount: String = "",
        val merchantName: String = "Custom Merchant",
        val cardLast4: String = "0000",
        val trustLevel: CustomerTrustLevel = CustomerTrustLevel.NEW,
        val amountError: String? = null,
        val isProcessing: Boolean = false
    )

    sealed interface Event {
        data class AmountChanged(val amount: String) : Event
        data class MerchantNameChanged(val name: String) : Event
        data class CardLast4Changed(val last4: String) : Event
        data class TrustLevelChanged(val level: CustomerTrustLevel) : Event
        data object Submit : Event
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

    fun onEvent(event: Event) {
        when (event) {
            is Event.AmountChanged -> onAmountChanged(event.amount)
            is Event.MerchantNameChanged -> onMerchantNameChanged(event.name)
            is Event.CardLast4Changed -> onCardLast4Changed(event.last4)
            is Event.TrustLevelChanged -> onTrustLevelChanged(event.level)
            is Event.Submit -> onSubmit()
        }
    }

    private fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount, amountError = null) }
    }

    private fun onMerchantNameChanged(name: String) {
        _uiState.update { it.copy(merchantName = name) }
    }

    private fun onCardLast4Changed(last4: String) {
        _uiState.update { it.copy(cardLast4 = last4) }
    }

    private fun onTrustLevelChanged(level: CustomerTrustLevel) {
        _uiState.update { it.copy(trustLevel = level) }
    }

    private fun onSubmit() {
        val currentState = _uiState.value
        if (currentState.isProcessing) return

        val amount = currentState.amount
        if (amount.isBlank()) {
            _uiState.update { it.copy(amountError = "Amount is required") }
            return
        }

        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid positive amount") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, amountError = null) }

        val transactionId = "custom-${System.currentTimeMillis()}"
        val sdkTransaction = Transaction(
            id = transactionId,
            amount = parsedAmount,
            currency = "USD",
            merchantName = currentState.merchantName,
            cardLast4 = currentState.cardLast4,
            customerTrustLevel = currentState.trustLevel,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
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
}
