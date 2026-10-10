package com.belectronicsvp.ui.navigation

sealed class Screen(val route: String) {
    data object DashboardList : Screen("dashboard_list")
    data class DashboardDetail(val panelId: String) : Screen("dashboard_detail/{panelId}") {
        companion object {
            const val ROUTE = "dashboard_detail/{panelId}"
            fun createRoute(panelId: String) = "dashboard_detail/$panelId"
        }
    }
}
