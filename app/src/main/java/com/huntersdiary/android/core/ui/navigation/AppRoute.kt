package com.huntersdiary.android.core.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Main : AppRoute("main")
}
