package com.example.moneytrees1.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    fun getRecentExpenses(userId: Int): Flow<List<ExpenseEntity>> {
        return expenseDao.getRecentExpenses(userId)
    }

    suspend fun getAllExpenses(userId: Int): List<ExpenseEntity> {
        return expenseDao.getAllExpenses(userId)
    }

    suspend fun getExpensesByCategory(userId: Int, category: String): List<ExpenseEntity> {
        return expenseDao.getExpensesByCategory(userId, category)
    }

    fun getTotalExpenses(userId: Int): Flow<Double> {
        return expenseDao.getTotalExpenses(userId)
    }

    suspend fun getExpensesBetweenDates(userId: Int, start: String, end: String): List<ExpenseEntity> {
        return expenseDao.getExpensesBetweenDates(userId, start, end)
    }

    // Add userId parameter
    fun getAllExpensesFlow(userId: Int): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpensesFlow(userId)
    }

    // Add this to clear user-specific expenses
    suspend fun clearUserExpenses(userId: Int) {
        expenseDao.clearUserExpenses(userId)
    }
}