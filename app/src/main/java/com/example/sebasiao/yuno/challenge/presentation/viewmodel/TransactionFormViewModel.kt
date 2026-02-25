package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TransactionFormViewModel : ViewModel() {

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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
    }
}
