package com.tinygc.okodukai.di

import com.tinygc.okodukai.data.repository.BudgetRepositoryImpl
import com.tinygc.okodukai.data.repository.CategoryRepositoryImpl
import com.tinygc.okodukai.data.repository.ExpenseRepositoryImpl
import com.tinygc.okodukai.data.repository.IncomeRepositoryImpl
import com.tinygc.okodukai.data.repository.TemplateRepositoryImpl
import com.tinygc.okodukai.domain.repository.BudgetRepository
import com.tinygc.okodukai.domain.repository.CategoryRepository
import com.tinygc.okodukai.domain.repository.ExpenseRepository
import com.tinygc.okodukai.domain.repository.IncomeRepository
import com.tinygc.okodukai.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * リポジトリのDIモジュール
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        incomeRepositoryImpl: IncomeRepositoryImpl
    ): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        templateRepositoryImpl: TemplateRepositoryImpl
    ): TemplateRepository
}
