package com.example.computingsystem.presentation.navigation

sealed class Screen(val route: String) {
    data object Calculator : Screen("calculator")
    data object Board     : Screen("board")
    data object Settings  : Screen("settings")
}