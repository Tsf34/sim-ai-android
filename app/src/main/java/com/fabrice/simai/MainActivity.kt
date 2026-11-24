package com.fabrice.simai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fabrice.simai.data.ConversationRepository
import com.fabrice.simai.data.SettingsRepository
import com.fabrice.simai.data.api.OpenCodeApiClient
import com.fabrice.simai.data.db.SimAiDatabase
import com.fabrice.simai.ui.screens.ChatScreen
import com.fabrice.simai.ui.screens.SettingsScreen
import com.fabrice.simai.ui.screens.WelcomeScreen
import com.fabrice.simai.ui.theme.SimAiTheme
import com.fabrice.simai.viewmodel.ChatViewModel
import com.fabrice.simai.viewmodel.SettingsViewModel

/** Écran principal (launcher). */
enum class Screen(val route: String) {
    WELCOME("welcome"),
    CHAT("chat"),
    SETTINGS("settings")
}

class MainActivity : ComponentActivity() {

    private val appContext = applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val apiClient = OpenCodeApiClient(settingsRepository)
    private val conversationRepository = ConversationRepository(SimAiDatabase.getDatabase(appContext))

    private val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(settingsRepository, apiClient)
    }

    private val chatViewModel: ChatViewModel by lazy {
        ChatViewModel(apiClient, settingsRepository, conversationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimAiTheme {
                val navController = rememberNavController()
                val chat = remember { chatViewModel }
                val settings = remember { settingsViewModel }

                NavHost(navController, startDestination = Screen.WELCOME.route) {
                    composable(Screen.WELCOME.route) {
                        WelcomeScreen(onStart = { navController.navigate(Screen.CHAT.route) })
                    }
                    composable(Screen.CHAT.route) {
                        ChatScreen(
                            viewModel = chat,
                            onNavigateToSettings = { navController.navigate(Screen.SETTINGS.route) }
                        )
                    }
                    composable(Screen.SETTINGS.route) {
                        SettingsScreen(
                            viewModel = settings,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
