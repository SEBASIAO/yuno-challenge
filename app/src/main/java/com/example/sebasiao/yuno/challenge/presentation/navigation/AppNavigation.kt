package com.example.sebasiao.yuno.challenge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
                TransactionListViewModel(
                    appContainer.getSampleTransactions,
                    appContainer.getSampleTransactionById,
                    appContainer.authResultHolder
                )
            }
            TransactionListScreen(
                viewModel = viewModel,
                onNavigateToResult = { transactionId ->
                    navController.navigate(Screen.Result.createRoute(transactionId)) {
                        launchSingleTop = true
                    }
                },
                onCustomTransactionClick = {
                    navController.navigate(Screen.TransactionForm.route)
                }
            )
        }

        composable(Screen.TransactionForm.route) {
            val viewModel: TransactionFormViewModel = viewModel {
                TransactionFormViewModel(appContainer.authResultHolder)
            }
            TransactionFormScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToResult = { transactionId ->
                    navController.navigate(Screen.Result.createRoute(transactionId)) {
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
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            val viewModel: ResultViewModel = viewModel {
                ResultViewModel(appContainer.getSampleTransactionById)
            }

            LaunchedEffect(transactionId) {
                val held = appContainer.authResultHolder.get()
                if (held != null) {
                    viewModel.loadResult(transactionId, held.second)
                }
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
