package com.example.sebasiao.yuno.challenge.presentation.viewmodel

import app.cash.turbine.test
import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.domain.usecase.GetSampleTransactionByIdUseCase
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.RiskAssessment
import com.yuno.payments.threeds.domain.model.RiskFactorResult
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getSampleTransactionById: GetSampleTransactionByIdUseCase

    private val sampleTransaction = SampleTransaction(
        id = "1",
        amount = 25.0,
        currency = "USD",
        merchantName = "Coffee Shop",
        cardLast4 = "1234",
        customerTrustLevel = CustomerTrustLevel.TRUSTED,
        scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
        scenarioDescription = "Low risk frictionless flow"
    )

    private val sampleResult = AuthenticationResult(
        status = AuthenticationStatus.AUTHENTICATED_FRICTIONLESS,
        decision = AuthenticationDecision(
            riskAssessment = RiskAssessment(
                score = 15,
                riskLevel = RiskLevel.LOW,
                factorResults = listOf(
                    RiskFactorResult(
                        name = "Amount",
                        score = 10,
                        weight = 0.3,
                        description = "Low amount"
                    ),
                    RiskFactorResult(
                        name = "Trust",
                        score = 5,
                        weight = 0.4,
                        description = "Trusted customer"
                    )
                )
            ),
            action = AuthenticationAction.FRICTIONLESS,
            policyApplied = RiskPolicy.default()
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSampleTransactionById = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() = runTest {
        val viewModel = ResultViewModel(getSampleTransactionById)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ResultViewModel.UiState.Loading)
        }
    }

    @Test
    fun loadResult_emitsSuccessState() = runTest {
        every { getSampleTransactionById("1") } returns sampleTransaction

        val viewModel = ResultViewModel(getSampleTransactionById)

        viewModel.uiState.test {
            awaitItem() // Loading state

            viewModel.loadResult("1", sampleResult)

            val successState = awaitItem()
            assertTrue(successState is ResultViewModel.UiState.Success)
            val success = successState as ResultViewModel.UiState.Success
            assertNotNull(success.transaction)
            assertEquals("Coffee Shop", success.transaction?.merchantName)
            assertEquals(AuthenticationStatus.AUTHENTICATED_FRICTIONLESS, success.result.status)
            assertEquals(RiskLevel.LOW, success.result.decision.riskAssessment.riskLevel)
            assertEquals(2, success.result.decision.riskAssessment.factorResults.size)
        }
    }

    @Test
    fun loadResult_withUnknownId_emitsSuccessWithNullTransaction() = runTest {
        every { getSampleTransactionById("unknown") } returns null

        val viewModel = ResultViewModel(getSampleTransactionById)

        viewModel.uiState.test {
            awaitItem() // Loading state

            viewModel.loadResult("unknown", sampleResult)

            val successState = awaitItem()
            assertTrue(successState is ResultViewModel.UiState.Success)
            val success = successState as ResultViewModel.UiState.Success
            assertNull(success.transaction)
            assertEquals(AuthenticationStatus.AUTHENTICATED_FRICTIONLESS, success.result.status)
        }
    }
}
