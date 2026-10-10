package com.belectronicsvp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.belectronicsvp.BElectronicsApp
import com.belectronicsvp.ui.dashboard.DashboardDetailScreen
import com.belectronicsvp.ui.dashboard.DashboardDetailViewModel
import com.belectronicsvp.ui.dashboardlist.DashboardListScreen
import com.belectronicsvp.ui.dashboardlist.DashboardListViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    app: BElectronicsApp
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DashboardList.route
    ) {
        composable(Screen.DashboardList.route) {
            val listViewModel = remember {
                DashboardListViewModel(app.panelRepository)
            }
            DashboardListScreen(
                viewModel = listViewModel,
                onOpenPanel = { panelId ->
                    navController.navigate(Screen.DashboardDetail.createRoute(panelId))
                }
            )
        }

        composable(
            route = Screen.DashboardDetail.ROUTE,
            arguments = listOf(
                navArgument("panelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val panelId = backStackEntry.arguments?.getString("panelId") ?: return@composable
            val detailViewModel = remember(panelId) {
                DashboardDetailViewModel(
                    panelId = panelId,
                    repository = app.panelRepository,
                    classicManager = app.bluetoothClassicManager,
                    bleManager = app.universalBleManager,
                    lastDeviceStore = app.lastDeviceStore,
                    protocolParser = app.protocolParser
                )
            }
            DashboardDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
