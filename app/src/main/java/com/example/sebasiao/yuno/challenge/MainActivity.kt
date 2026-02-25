package com.example.sebasiao.yuno.challenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.sebasiao.yuno.challenge.presentation.navigation.AppNavHost
import com.example.sebasiao.yuno.challenge.presentation.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as MerchantApp).container
        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    appContainer = container
                )
            }
        }
    }
}
