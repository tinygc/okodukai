package com.tinygc.okodukai.presentation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tinygc.okodukai.presentation.navigation.BottomNavDestination
import com.tinygc.okodukai.presentation.screen.BudgetSettingScreen
import com.tinygc.okodukai.presentation.screen.CategoryManagementScreen
import com.tinygc.okodukai.presentation.screen.ExpenseEntryScreen
import com.tinygc.okodukai.presentation.screen.IncomeManagementScreen
import com.tinygc.okodukai.presentation.screen.ManagementHubScreen
import com.tinygc.okodukai.presentation.screen.MonthlyHistoryScreen
import com.tinygc.okodukai.presentation.screen.MonthlySummaryScreen
import com.tinygc.okodukai.presentation.screen.TemplateManagementScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavDestination.Expense,
        BottomNavDestination.Summary,
        BottomNavDestination.Management
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(destination.route) {
                                    popUpTo(BottomNavDestination.Expense.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = destination.icon,
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Expense.route,
            modifier = Modifier
        ) {
            composable(BottomNavDestination.Expense.route) {
                ExpenseEntryScreen(paddingValues = paddingValues)
            }
            composable(BottomNavDestination.Summary.route) {
                MonthlySummaryScreen(paddingValues = paddingValues)
            }
            composable(BottomNavDestination.Management.route) {
                ManagementHubScreen(
                    paddingValues = paddingValues,
                    onNavigateToCategory = { navController.navigate("category_management") },
                    onNavigateToTemplate = { navController.navigate("template_management") },
                    onNavigateToBudget = { navController.navigate("budget_setting") },
                    onNavigateToIncome = { navController.navigate("income_management") },
                    onNavigateToHistory = { navController.navigate("monthly_history") }
                )
            }
            composable("category_management") {
                CategoryManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("template_management") {
                TemplateManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("budget_setting") {
                BudgetSettingScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("income_management") {
                IncomeManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("monthly_history") {
                MonthlyHistoryScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
