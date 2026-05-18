package com.example.computingsystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.computingsystem.presentation.calculator.CalculatorScreen
import com.example.computingsystem.presentation.board.BoardScreen
import com.example.computingsystem.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route,
        modifier = modifier
    ) {
        composable(Screen.Calculator.route) { CalculatorScreen() }
        composable(Screen.Board.route)      { BoardScreen() }
        composable(Screen.Settings.route)   { SettingsScreen() }
    }
}