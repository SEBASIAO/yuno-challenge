package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import app.cash.turbine.test
import com.example.sebasiao.yuno.challenge.di.AuthenticationResultHolder
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionsUseCase
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getSampleTransactions: GetSampleTransactionsUseCase
    private lateinit var getSampleTransactionById: GetSampleTransactionByIdUseCase
    private lateinit var resultHolder: AuthenticationResultHolder

    private val sampleTransactions = listOf(
        SampleTransaction(
            id = "1",
            amount = 25.0,
            currency = "USD",
            merchantName = "Coffee Shop",
            cardLast4 = "1234",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
            scenarioDescription = "Low risk frictionless flow"
        ),
        SampleTransaction(
            id = "2",
            amount = 500.0,
            currency = "USD",
            merchantName = "Electronics Store",
            cardLast4 = "5678",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Medium risk challenge flow"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSampleTransactions = mockk()
        getSampleTransactionById = mockk()
        resultHolder = AuthenticationResultHolder()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionListViewModel {
        return TransactionListViewModel(
            getSampleTransactions,
            getSampleTransactionById,
            resultHolder
        )
    }

    @Test
    fun loadTransactions_success_emitsTransactionList() = runTest {
        every { getSampleTransactions() } returns sampleTransactions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.transactions.size)
            assertEquals("Coffee Shop", state.transactions[0].merchantName)
            assertEquals("Electronics Store", state.transactions[1].merchantName)
        }
    }

    @Test
    fun initialState_defaultPolicyIsPolicyA() = runTest {
        every { getSampleTransactions() } returns sampleTransactions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TransactionListViewModel.PolicyOption.POLICY_A, state.activePolicy)
        }
    }

    @Test
    fun onPolicyToggle_switchesToPolicyB() = runTest {
        every { getSampleTransactions() } returns sampleTransactions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial loaded state

            viewModel.onEvent(
                TransactionListViewModel.Event.TogglePolicy(
                    TransactionListViewModel.PolicyOption.POLICY_B
                )
            )

            val updatedState = awaitItem()
            assertEquals(TransactionListViewModel.PolicyOption.POLICY_B, updatedState.activePolicy)
        }
    }

    @Test
    fun onPolicyToggle_switchesBackToPolicyA() = runTest {
        every { getSampleTransactions() } returns sampleTransactions

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // initial loaded state

            viewModel.onEvent(
                TransactionListViewModel.Event.TogglePolicy(
                    TransactionListViewModel.PolicyOption.POLICY_B
                )
            )
            awaitItem() // policy B state

            viewModel.onEvent(
                TransactionListViewModel.Event.TogglePolicy(
                    TransactionListViewModel.PolicyOption.POLICY_A
                )
            )
            val finalState = awaitItem()
            assertEquals(TransactionListViewModel.PolicyOption.POLICY_A, finalState.activePolicy)
        }
    }

    @Test
    fun loadTransactions_emptyList_emitsEmptyState() = runTest {
        every { getSampleTransactions() } returns emptyList()

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.transactions.isEmpty())
        }
    }
}
