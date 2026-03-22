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
}
