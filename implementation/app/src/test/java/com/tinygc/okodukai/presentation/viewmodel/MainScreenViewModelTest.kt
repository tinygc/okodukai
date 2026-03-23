package com.tinygc.okodukai.presentation.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun `FR-14 hideフラグが未読込ならダイアログを表示しないこと`() {
        val result = calculateShouldShowInitialSetupDialog(
            hideFlag = null,
            templateVisited = false,
            budgetExists = false
        )

        assertFalse(result)
    }

    @Test
    fun `FR-14 hideフラグがtrueならダイアログを表示しないこと`() {
        val result = calculateShouldShowInitialSetupDialog(
            hideFlag = true,
            templateVisited = false,
            budgetExists = false
        )

        assertFalse(result)
    }

    @Test
    fun `FR-14 予算未設定ならダイアログを表示すること`() {
        val result = calculateShouldShowInitialSetupDialog(
            hideFlag = false,
            templateVisited = true,
            budgetExists = false
        )

        assertTrue(result)
    }

    @Test
    fun `FR-14 テンプレ未訪問ならダイアログを表示すること`() {
        val result = calculateShouldShowInitialSetupDialog(
            hideFlag = false,
            templateVisited = false,
            budgetExists = true
        )

        assertTrue(result)
    }

    @Test
    fun `FR-14 予算設定済みかつテンプレ訪問済みならダイアログを表示しないこと`() {
        val result = calculateShouldShowInitialSetupDialog(
            hideFlag = false,
            templateVisited = true,
            budgetExists = true
        )

        assertFalse(result)
    }
}