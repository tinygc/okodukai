package com.tinygc.okodukai.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenRouteTest {

    @Test
    fun buildCategoryListRouteIncludesMonthParameter() {
        val month = "2026-02"

        val route = buildCategoryListRoute(month)

        assertEquals("category_list/2026-02", route)
    }

    @Test
    fun resolveMonthArgFallsBackToCurrentMonthWhenInvalid() {
        val currentMonth = com.tinygc.okodukai.domain.util.DateTimeUtil.getCurrentMonth()

        val resolved = resolveMonthArg("2026/02")

        assertEquals(currentMonth, resolved)
    }


    @Test
    fun initialSetupRouteBudgetReturnsBudgetRoute() {
        val route = initialSetupRoute(InitialSetupDestination.BUDGET)

        assertEquals("budget_setting", route)
    }

    @Test
    fun initialSetupRouteTemplateReturnsTemplateRoute() {
        val route = initialSetupRoute(InitialSetupDestination.TEMPLATE)

        assertEquals("template_management", route)
    }

    @Test
    fun shouldRenderInitialSetupDialogShowsWhenForced() {
        val result = shouldRenderInitialSetupDialog(
            shouldShowByRule = false,
            isOnMainTab = true,
            dismissUntilLeaveMainTabs = false,
            forceShowDialog = true
        )

        assertEquals(true, result)
    }

    @Test
    fun shouldRenderInitialSetupDialogHidesWhenDismissedInMainTabs() {
        val result = shouldRenderInitialSetupDialog(
            shouldShowByRule = true,
            isOnMainTab = true,
            dismissUntilLeaveMainTabs = true,
            forceShowDialog = false
        )

        assertEquals(false, result)
    }

    @Test
    fun shouldRenderInitialSetupDialogHidesOutsideMainTabs() {
        val result = shouldRenderInitialSetupDialog(
            shouldShowByRule = true,
            isOnMainTab = false,
            dismissUntilLeaveMainTabs = false,
            forceShowDialog = false
        )

        assertEquals(false, result)
    }
}
