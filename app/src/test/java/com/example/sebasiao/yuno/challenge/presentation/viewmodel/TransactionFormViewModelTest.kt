package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import app.cash.turbine.test
import com.example.sebasiao.yuno.challenge.di.AuthenticationResultHolder
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionFormViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var resultHolder: AuthenticationResultHolder

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        resultHolder = AuthenticationResultHolder()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionFormViewModel {
        return TransactionFormViewModel(resultHolder)
    }

    @Test
    fun initialState_hasEmptyAmount() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.amount)
            assertEquals("Custom Merchant", state.merchantName)
            assertEquals("0000", state.cardLast4)
            assertEquals(CustomerTrustLevel.NEW, state.trustLevel)
            assertNull(state.amountError)
            assertFalse(state.isProcessing)
        }
    }

    @Test
    fun onAmountChanged_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.AmountChanged("150.00"))

            val updatedState = awaitItem()
            assertEquals("150.00", updatedState.amount)
            assertNull(updatedState.amountError)
        }
    }

    @Test
    fun onMerchantNameChanged_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.MerchantNameChanged("My Shop"))

            val updatedState = awaitItem()
            assertEquals("My Shop", updatedState.merchantName)
        }
    }

    @Test
    fun onCardLast4Changed_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.CardLast4Changed("9999"))

            val updatedState = awaitItem()
            assertEquals("9999", updatedState.cardLast4)
        }
    }

    @Test
    fun onTrustLevelChanged_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(
                TransactionFormViewModel.Event.TrustLevelChanged(CustomerTrustLevel.TRUSTED)
            )

            val updatedState = awaitItem()
            assertEquals(CustomerTrustLevel.TRUSTED, updatedState.trustLevel)
        }
    }

    @Test
    fun onSubmit_withEmptyAmount_showsError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.Submit)

            val errorState = awaitItem()
            assertNotNull(errorState.amountError)
            assertEquals("Amount is required", errorState.amountError)
            assertFalse(errorState.isProcessing)
        }
    }

    @Test
    fun onSubmit_withInvalidAmount_showsError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.AmountChanged("abc"))
            awaitItem() // amount changed state

            viewModel.onEvent(TransactionFormViewModel.Event.Submit)

            val errorState = awaitItem()
            assertNotNull(errorState.amountError)
            assertEquals("Enter a valid positive amount", errorState.amountError)
            assertFalse(errorState.isProcessing)
        }
    }

    @Test
    fun onSubmit_withNegativeAmount_showsError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.AmountChanged("-50"))
            awaitItem() // amount changed state

            viewModel.onEvent(TransactionFormViewModel.Event.Submit)

            val errorState = awaitItem()
            assertNotNull(errorState.amountError)
            assertEquals("Enter a valid positive amount", errorState.amountError)
        }
    }

    @Test
    fun onSubmit_withZeroAmount_showsError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(TransactionFormViewModel.Event.AmountChanged("0"))
            awaitItem() // amount changed state

            viewModel.onEvent(TransactionFormViewModel.Event.Submit)

            val errorState = awaitItem()
            assertNotNull(errorState.amountError)
            assertEquals("Enter a valid positive amount", errorState.amountError)
        }
    }

    @Test
    fun onAmountChanged_clearsError() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial state

            // Trigger error
            viewModel.onEvent(TransactionFormViewModel.Event.Submit)
            val errorState = awaitItem()
            assertNotNull(errorState.amountError)

            // Change amount should clear error
            viewModel.onEvent(TransactionFormViewModel.Event.AmountChanged("50"))
            val clearedState = awaitItem()
            assertNull(clearedState.amountError)
        }
    }
}
