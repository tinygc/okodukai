package com.tinygc.okodukai.presentation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinygc.okodukai.domain.util.DateTimeUtil
import com.tinygc.okodukai.presentation.navigation.BottomNavDestination
import com.tinygc.okodukai.presentation.screen.BudgetSettingScreen
import com.tinygc.okodukai.presentation.screen.BackupManagementScreen
import com.tinygc.okodukai.presentation.screen.CategoryManagementScreen
import com.tinygc.okodukai.presentation.screen.CategoryListScreen
import com.tinygc.okodukai.presentation.screen.DefaultCategorySettingScreen
import com.tinygc.okodukai.presentation.screen.ExpenseEntryScreen
import com.tinygc.okodukai.presentation.screen.ExpenseListScreen
import com.tinygc.okodukai.presentation.screen.IncomeManagementScreen
import com.tinygc.okodukai.presentation.screen.ManagementHubScreen
import com.tinygc.okodukai.presentation.screen.MonthlyHistoryScreen
import com.tinygc.okodukai.presentation.screen.MonthlySummaryScreen
import com.tinygc.okodukai.presentation.screen.QuickAmountSettingScreen
import com.tinygc.okodukai.presentation.screen.SavingGoalManagementScreen
import com.tinygc.okodukai.presentation.screen.TemplateManagementScreen
import com.tinygc.okodukai.presentation.screen.RemoveAdsScreen
import com.tinygc.okodukai.presentation.viewmodel.MainScreenViewModel
import com.tinygc.okodukai.presentation.component.AdBanner
import com.tinygc.okodukai.domain.repository.BillingRepository
import androidx.compose.material3.HorizontalDivider

private val monthArgRegex = Regex("^\\d{4}-\\d{2}$")
private const val ROUTE_BUDGET_SETTING = "budget_setting"
private const val ROUTE_TEMPLATE_MANAGEMENT = "template_management"

internal enum class InitialSetupDestination {
    BUDGET,
    TEMPLATE
}

internal fun resolveMonthArg(rawMonth: String?): String {
    val fallback = DateTimeUtil.getCurrentMonth()
    return if (rawMonth != null && monthArgRegex.matches(rawMonth)) rawMonth else fallback
}

internal fun buildCategoryListRoute(month: String): String = "category_list/${resolveMonthArg(month)}"
internal fun buildExpenseListRoute(month: String): String = "expense_list/${resolveMonthArg(month)}"
internal fun shouldRenderInitialSetupDialog(
    shouldShowByRule: Boolean,
    isOnMainTab: Boolean,
    dismissUntilLeaveMainTabs: Boolean,
    forceShowDialog: Boolean
): Boolean {
    return isOnMainTab && !dismissUntilLeaveMainTabs && (forceShowDialog || shouldShowByRule)
}

internal fun initialSetupRoute(destination: InitialSetupDestination): String {
    return when (destination) {
        InitialSetupDestination.BUDGET -> ROUTE_BUDGET_SETTING
        InitialSetupDestination.TEMPLATE -> ROUTE_TEMPLATE_MANAGEMENT
    }
}

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = hiltViewModel(),
    billingRepository: BillingRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val shouldShowDialog by viewModel.shouldShowInitialSetupDialog.collectAsState()
    val isAdRemovalPurchased by billingRepository.isAdRemovalPurchased.collectAsState(initial = false)
    var doNotShowAgain by rememberSaveable { mutableStateOf(false) }
    var dismissInitialSetupUntilLeaveMainTabs by rememberSaveable { mutableStateOf(false) }
    var forceShowInitialSetupDialog by rememberSaveable { mutableStateOf(false) }
    val items = listOf(
        BottomNavDestination.Expense,
        BottomNavDestination.Summary,
        BottomNavDestination.Management
    )
    val isOnMainTab = currentDestination?.hierarchy?.any { destination ->
        items.any { it.route == destination.route }
    } == true
    val showInitialSetupDialog = shouldRenderInitialSetupDialog(
        shouldShowByRule = shouldShowDialog,
        isOnMainTab = isOnMainTab,
        dismissUntilLeaveMainTabs = dismissInitialSetupUntilLeaveMainTabs,
        forceShowDialog = forceShowInitialSetupDialog
    )

    LaunchedEffect(isOnMainTab) {
        if (!isOnMainTab) {
            dismissInitialSetupUntilLeaveMainTabs = false
            forceShowInitialSetupDialog = false
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                if (!isAdRemovalPurchased) {
                    HorizontalDivider()
                    AdBanner()
                }
                NavigationBar {
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
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
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
                MonthlySummaryScreen(
                    paddingValues = paddingValues,
                    onNavigateToExpenseList = { month -> navController.navigate(buildExpenseListRoute(month)) },
                    onNavigateToCategoryList = { month -> navController.navigate(buildCategoryListRoute(month)) }
                )
            }
            composable("category_list/{month}") { backStackEntry ->
                val month = resolveMonthArg(backStackEntry.arguments?.getString("month"))
                CategoryListScreen(
                    paddingValues = paddingValues,
                    month = month,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("expense_list/{month}") { backStackEntry ->
                val month = resolveMonthArg(backStackEntry.arguments?.getString("month"))
                ExpenseListScreen(
                    paddingValues = paddingValues,
                    month = month,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavDestination.Management.route) {
                ManagementHubScreen(
                    paddingValues = paddingValues,
                    onNavigateToCategory = { navController.navigate("category_management") },
                    onNavigateToTemplate = { navController.navigate(ROUTE_TEMPLATE_MANAGEMENT) },
                    onNavigateToBudget = { navController.navigate(ROUTE_BUDGET_SETTING) },
                    onNavigateToIncome = { navController.navigate("income_management") },
                    onNavigateToHistory = { navController.navigate("monthly_history") },
                    onNavigateToDefaultCategory = { navController.navigate("default_category_setting") },
                    onNavigateToSavingGoal = { navController.navigate("saving_goal_management") },
                    onNavigateToQuickAmountSetting = { navController.navigate("quick_amount_setting") },
                    onNavigateToBackup = { navController.navigate("backup_management") },
                    onNavigateToRemoveAds = { navController.navigate("remove_ads") },
                    isAdRemovalPurchased = isAdRemovalPurchased,
                    onShowInitialSetupGuide = {
                        viewModel.showInitialSetupAnnouncementAgain()
                        doNotShowAgain = false
                        dismissInitialSetupUntilLeaveMainTabs = false
                        forceShowInitialSetupDialog = true
                    }
                )
            }
            composable("category_management") {
                CategoryManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_TEMPLATE_MANAGEMENT) {
                TemplateManagementScreen(
                    paddingValues = paddingValues,
                    onVisited = { viewModel.markTemplateManagementVisited() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_BUDGET_SETTING) {
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
            composable("default_category_setting") {
                DefaultCategorySettingScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("saving_goal_management") {
                SavingGoalManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("backup_management") {
                BackupManagementScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("quick_amount_setting") {
                QuickAmountSettingScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("remove_ads") {
                RemoveAdsScreen(
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showInitialSetupDialog) {
        val applyHideSelectionAndClose = {
            dismissInitialSetupUntilLeaveMainTabs = true
            forceShowInitialSetupDialog = false
            if (doNotShowAgain) {
                viewModel.hideInitialSetupAnnouncement()
            }
            doNotShowAgain = false
        }

        AlertDialog(
            onDismissRequest = applyHideSelectionAndClose,
            title = { Text("セットアップ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "最初に予算を設定しておくと、月の管理がしやすくなります。テンプレを使うと入力も早くなります。"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            applyHideSelectionAndClose()
                            navController.navigate(initialSetupRoute(InitialSetupDestination.BUDGET))
                        }
                    ) {
                        Text("予算設定へ")
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            applyHideSelectionAndClose()
                            navController.navigate(initialSetupRoute(InitialSetupDestination.TEMPLATE))
                        }
                    ) {
                        Text("テンプレ管理へ")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = doNotShowAgain,
                            onCheckedChange = { checked -> doNotShowAgain = checked }
                        )
                        Text("今後表示しない")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = applyHideSelectionAndClose
                ) {
                    Text("あとで")
                }
            }
        )
    }
}
