package com.tinygc.okodukai.data.local.mapper

import com.tinygc.okodukai.data.local.entity.BudgetEntity
import com.tinygc.okodukai.data.local.entity.CategoryEntity
import com.tinygc.okodukai.data.local.entity.CategoryOrderEntity
import com.tinygc.okodukai.data.local.entity.ExpenseEntity
import com.tinygc.okodukai.data.local.entity.IncomeEntity
import com.tinygc.okodukai.data.local.entity.TemplateEntity
import com.tinygc.okodukai.domain.model.Budget
import com.tinygc.okodukai.domain.model.Category
import com.tinygc.okodukai.domain.model.CategoryOrder
import com.tinygc.okodukai.domain.model.Expense
import com.tinygc.okodukai.domain.model.Income
import com.tinygc.okodukai.domain.model.Template

/**
 * Entity <-> Domain Model マッパー
 */

// Budget
fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    month = month,
    amount = amount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    month = month,
    amount = amount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Expense
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    date = date,
    amount = amount,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    memo = memo,
    isUncategorized = isUncategorized,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    date = date,
    amount = amount,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    memo = memo,
    isUncategorized = isUncategorized,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Income
fun IncomeEntity.toDomain(): Income = Income(
    id = id,
    date = date,
    amount = amount,
    memo = memo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Income.toEntity(): IncomeEntity = IncomeEntity(
    id = id,
    date = date,
    amount = amount,
    memo = memo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Category
fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// CategoryOrder
fun CategoryOrderEntity.toDomain(): CategoryOrder = CategoryOrder(
    id = id,
    categoryId = categoryId,
    parentId = parentId,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CategoryOrder.toEntity(): CategoryOrderEntity = CategoryOrderEntity(
    id = id,
    categoryId = categoryId,
    parentId = parentId,
    displayOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Template
fun TemplateEntity.toDomain(): Template = Template(
    id = id,
    name = name,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    amount = amount,
    sortOrder = displayOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Template.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    amount = amount,
    displayOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)
