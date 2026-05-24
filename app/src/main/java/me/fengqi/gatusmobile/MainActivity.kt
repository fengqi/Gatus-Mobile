package me.fengqi.gatusmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import me.fengqi.gatusmobile.navigation.AppNavGraph
import me.fengqi.gatusmobile.ui.theme.GatusTheme
import me.fengqi.gatusmobile.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GatusTheme {
                val settingsViewModel: SettingsViewModel = viewModel()
                LaunchedEffect(Unit) {
                    settingsViewModel.init(this@MainActivity)
                }

                val serverUrl by settingsViewModel.serverUrl.collectAsState()
                val isConfigured by settingsViewModel.isConfigured.collectAsState()

                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    serverUrl = serverUrl,
                    isConfigured = isConfigured,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
