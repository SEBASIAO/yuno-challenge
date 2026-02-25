package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ResultViewModel(
    private val getSampleTransactionById: GetSampleTransactionByIdUseCase
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val transaction: SampleTransaction?,
            val result: AuthenticationResult
        ) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadResult(transactionId: String, result: AuthenticationResult) {
        val transaction = getSampleTransactionById(transactionId)
        _uiState.update { UiState.Success(transaction = transaction, result = result) }
    }
}
