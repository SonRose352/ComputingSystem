package com.example.computingsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.computingsystem.domain.model.AppLanguage
import com.example.computingsystem.domain.model.AppTheme
import com.example.computingsystem.presentation.navigation.AppNavGraph
import com.example.computingsystem.presentation.navigation.Screen
import com.example.computingsystem.presentation.settings.SettingsViewModel
import com.example.computingsystem.ui.theme.ComputingSystemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            LaunchedEffect(settings.language) {

                val languageTag = when (settings.language) {
                    AppLanguage.RUSSIAN -> "ru"
                    AppLanguage.ENGLISH -> "en"
                }

                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag)
                )
            }

            val darkTheme = when (settings.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            ComputingSystemTheme(
                darkTheme = darkTheme
            ) {
                MainScaffold()
            }
        }
    }
}

@Composable
private fun MainScaffold() {

    val navController = rememberNavController()

    val navItems = listOf(
        NavItem(
            screen = Screen.Calculator,
            icon = ImageVector.vectorResource(R.drawable.ic_calculator),
            labelRes = R.string.nav_calculator
        ),
        NavItem(
            screen = Screen.Board,
            icon = ImageVector.vectorResource(R.drawable.ic_board),
            labelRes = R.string.nav_board
        ),
        NavItem(
            screen = Screen.Settings,
            icon = ImageVector.vectorResource(R.drawable.ic_settings),
            labelRes = R.string.nav_settings
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {

            NavigationBar {

                val backStack by navController.currentBackStackEntryAsState()
                val currentDest = backStack?.destination

                navItems.forEach { item ->

                    NavigationBarItem(
                        selected = currentDest?.hierarchy
                            ?.any { it.route == item.screen.route } == true,

                        onClick = {
                            navController.navigate(item.screen.route) {

                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.labelRes)
                            )
                        },

                        label = {
                            Text(
                                text = stringResource(item.labelRes)
                            )
                        }
                    )
                }
            }
        }

    ) { innerPadding ->

        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

private data class NavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelRes: Int
)