package com.example.sebasiao.yuno.challenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sebasiao.yuno.challenge.di.AppContainer
import com.example.sebasiao.yuno.challenge.presentation.ui.screen.ResultScreen
import com.example.sebasiao.yuno.challenge.presentation.ui.screen.TransactionFormScreen
import com.example.sebasiao.yuno.challenge.presentation.ui.screen.TransactionListScreen
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.ResultViewModel
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.TransactionFormViewModel
import com.example.sebasiao.yuno.challenge.presentation.viewmodel.TransactionListViewModel

sealed class Screen(val route: String) {
    data object TransactionList : Screen("transactions")
    data object TransactionForm : Screen("form")
    data object Result : Screen("result/{transactionId}") {
        fun createRoute(transactionId: String) = "result/$transactionId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TransactionList.route
    ) {
        composable(Screen.TransactionList.route) {
            val viewModel: TransactionListViewModel = viewModel {
                TransactionListViewModel(appContainer.getSampleTransactions)
            }
            TransactionListScreen(
                viewModel = viewModel,
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.Result.createRoute(transactionId))
                },
                onCustomTransactionClick = {
                    navController.navigate(Screen.TransactionForm.route)
                }
            )
        }

        composable(Screen.TransactionForm.route) {
            val viewModel: TransactionFormViewModel = viewModel {
                TransactionFormViewModel()
            }
            TransactionFormScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSubmit = { _ ->
                    navController.navigate(Screen.Result.createRoute("custom")) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            @Suppress("UNUSED_VARIABLE")
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val viewModel: ResultViewModel = viewModel {
                ResultViewModel(appContainer.getSampleTransactionById)
            }
            ResultScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack(
                        route = Screen.TransactionList.route,
                        inclusive = false
                    )
                }
            )
        }
    }
}
