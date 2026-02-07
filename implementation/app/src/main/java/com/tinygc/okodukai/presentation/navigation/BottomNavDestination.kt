package com.tinygc.okodukai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu

sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
) {
    data object Expense : BottomNavDestination(
        route = "expense",
        label = "支出入力",
        icon = { Icon(Icons.Filled.Edit, contentDescription = "支出入力") }
    )

    data object Summary : BottomNavDestination(
        route = "summary",
        label = "月次サマリ",
        icon = { Icon(Icons.Filled.Home, contentDescription = "月次サマリ") }
    )

    data object Management : BottomNavDestination(
        route = "management",
        label = "管理",
        icon = { Icon(Icons.Filled.Menu, contentDescription = "管理") }
    )
}
