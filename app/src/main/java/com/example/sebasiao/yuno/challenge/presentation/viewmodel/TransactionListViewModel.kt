package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TransactionListViewModel(
    private val getSampleTransactions: GetSampleTransactionsUseCase
) : ViewModel() {

    data class UiState(
        val transactions: List<SampleTransaction> = emptyList(),
        val activePolicy: PolicyOption = PolicyOption.POLICY_A,
        val isLoading: Boolean = true
    )

    enum class PolicyOption { POLICY_A, POLICY_B }

    sealed interface Event {
        data object LoadTransactions : Event
        data class TogglePolicy(val policy: PolicyOption) : Event
        data class TransactionClicked(val transactionId: String) : Event
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.LoadTransactions -> loadTransactions()
            is Event.TogglePolicy -> togglePolicy(event.policy)
            is Event.TransactionClicked -> { /* Navigation handled externally */ }
        }
    }

    private fun loadTransactions() {
        val transactions = getSampleTransactions()
        _uiState.update { it.copy(transactions = transactions, isLoading = false) }
    }

    private fun togglePolicy(policy: PolicyOption) {
        _uiState.update { it.copy(activePolicy = policy) }
    }
}
