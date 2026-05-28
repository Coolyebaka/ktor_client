package com.huntersdiary.android.core.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import com.huntersdiary.android.feature.auth.presentation.LoginScreen
import com.huntersdiary.android.feature.auth.presentation.RegisterScreen

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
) {
    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    if (state.isCheckingAuth) {
        LoadingScreen()
        return
    }

    val navController = rememberNavController()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && navController.currentDestination?.route != AppRoute.Main.route) {
            navController.navigate(AppRoute.Main.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (state.isAuthenticated) AppRoute.Main.route else AppRoute.Login.route,
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                state = state,
                onLogin = authViewModel::login,
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                state = state,
                onRegister = authViewModel::register,
                onLoginClick = { navController.popBackStack() },
            )
        }
        composable(AppRoute.Main.route) {
            MainPlaceholderScreen()
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MainPlaceholderScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Дневник охотника",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Главный экран",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
