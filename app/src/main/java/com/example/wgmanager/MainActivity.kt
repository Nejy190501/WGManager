package com.example.wgmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wgmanager.data.DataStore
import com.example.wgmanager.data.ThemeColor
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.screens.*
import com.example.wgmanager.ui.theme.WGManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Init mock data immediately (UI starts fast), then load from Firebase
        DataStore.initMockData()
        DataStore.initFromFirebase()

        setContent {
            var themeColor by remember { mutableStateOf(ThemeColor.INDIGO) }
            var isDark by remember { mutableStateOf(true) }

            WGManagerTheme(themeColor = themeColor, darkTheme = isDark) {
                val toastState = rememberToastState()
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

                val navigate: (AppScreen) -> Unit = { screen ->
                    // Sync theme with user prefs when navigating
                    DataStore.currentUser?.let { u ->
                        themeColor = u.themeColor
                        isDark = u.isDarkMode
                    }
                    currentScreen = screen
                }

                // Back navigation
                val mainScreens = setOf(AppScreen.SPLASH, AppScreen.LOGIN, AppScreen.DASHBOARD, AppScreen.SYSTEM_PANEL)
                BackHandler(enabled = currentScreen !in mainScreens) {
                    currentScreen = when (currentScreen) {
                        AppScreen.WG_FINDER -> AppScreen.LOGIN
                        else -> if (DataStore.isImpersonating()) {
                            DataStore.stopImpersonation()
                            AppScreen.SYSTEM_PANEL
                        } else AppScreen.DASHBOARD
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Phone shell: status bar
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (currentScreen != AppScreen.SPLASH) {
                            PhoneStatusBar()
                        }

                        // Screen content with cross-fade animation
                        AnimatedContent(
                            targetState = currentScreen,
                            modifier = Modifier.weight(1f),
                            transitionSpec = {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                            },
                            label = "screenTransition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.SPLASH -> SplashScreen(onFinished = { navigate(AppScreen.LOGIN) })
                                AppScreen.LOGIN -> LoginScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.WG_FINDER -> WGFinderScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.DASHBOARD -> DashboardScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.SHOPPING -> ShoppingScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.CLEANING -> CleaningScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.CREW -> CrewScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.CALENDAR -> CalendarScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.MEAL_PLANNER -> MealPlannerScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.VAULT -> VaultScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.REWARDS -> RewardsScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.ANALYTICS -> AnalyticsScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.BLACKBOARD -> BlackboardScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.PROFILE -> ProfileScreen(
                                    onNavigate = navigate, toast = toastState,
                                    onThemeChange = { themeColor = it },
                                    onDarkModeChange = { isDark = it }
                                )
                                AppScreen.SYSTEM_PANEL -> SystemPanelScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.RECURRING_COSTS -> RecurringCostsScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.WALL_OF_FAME -> WallOfFameScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.GUEST_PASS -> GuestPassScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.SMART_HOME -> SmartHomeScreen(onNavigate = navigate, toast = toastState)
                                AppScreen.ONBOARDING -> OnboardingScreen(onNavigate = navigate, toast = toastState)
                            }
                        }

                        // Home indicator
                        if (currentScreen != AppScreen.SPLASH) {
                            HomeIndicator()
                        }
                    }

                    // Toast overlay
                    WGToastHost(state = toastState, modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding())
                }
            }
        }
    }
}
