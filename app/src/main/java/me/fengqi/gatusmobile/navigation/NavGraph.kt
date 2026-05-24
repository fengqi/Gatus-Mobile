package me.fengqi.gatusmobile.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.fengqi.gatusmobile.ui.screen.DashboardScreen
import me.fengqi.gatusmobile.ui.screen.EndpointDetailScreen
import me.fengqi.gatusmobile.ui.screen.SettingsScreen
import me.fengqi.gatusmobile.ui.viewmodel.DashboardViewModel
import me.fengqi.gatusmobile.ui.viewmodel.EndpointDetailViewModel
import me.fengqi.gatusmobile.ui.viewmodel.SettingsViewModel
import me.fengqi.gatusmobile.ui.viewmodel.ValidationState

object Routes {
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val ENDPOINT_DETAIL = "endpoint_detail/{baseUrl}/{endpointKey}"

    fun endpointDetail(baseUrl: String, endpointKey: String): String {
        return "endpoint_detail/${java.net.URLEncoder.encode(baseUrl, "UTF-8")}/${java.net.URLEncoder.encode(endpointKey, "UTF-8")}"
    }
}

private const val ANIM_DURATION = 200

@Composable
fun AppNavGraph(
    navController: NavHostController,
    serverUrl: String,
    isConfigured: Boolean,
    settingsViewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val validationState by settingsViewModel.validationState.collectAsState()

    LaunchedEffect(validationState) {
        if (validationState is ValidationState.Success) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.SETTINGS) { inclusive = true }
            }
        }
    }

    val startDestination = if (isConfigured) Routes.DASHBOARD else Routes.SETTINGS

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Routes.DASHBOARD,
            exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { -it } },
            popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } }
        ) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            LaunchedEffect(serverUrl) {
                dashboardViewModel.init(serverUrl)
            }
            DashboardScreen(
                viewModel = dashboardViewModel,
                onEndpointClick = { key ->
                    navController.navigate(Routes.endpointDetail(serverUrl, key))
                }
            )
        }

        composable(
            route = Routes.ENDPOINT_DETAIL,
            arguments = listOf(
                navArgument("baseUrl") { type = NavType.StringType },
                navArgument("endpointKey") { type = NavType.StringType }
            ),
            enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
            popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
        ) { backStackEntry ->
            val baseUrl = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("baseUrl") ?: serverUrl, "UTF-8"
            )
            val endpointKey = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("endpointKey") ?: "", "UTF-8"
            )
            val detailViewModel: EndpointDetailViewModel = viewModel()
            EndpointDetailScreen(
                viewModel = detailViewModel,
                baseUrl = baseUrl,
                endpointKey = endpointKey,
                onBack = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) }
            )
        }

        composable(Routes.SETTINGS) {
            val context = LocalContext.current
            SettingsScreen(
                currentUrl = serverUrl,
                validationState = validationState,
                onSave = { url -> settingsViewModel.validateAndSave(context, url) },
                onUrlChanged = { settingsViewModel.resetValidation() },
                isInitialSetup = !isConfigured
            )
        }
    }
}
