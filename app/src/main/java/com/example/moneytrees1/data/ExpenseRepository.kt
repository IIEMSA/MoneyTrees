package com.example.moneytrees1.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    fun getRecentExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getRecentExpenses()
    }

    suspend fun getAllExpenses(): List<ExpenseEntity> {
        return expenseDao.getAllExpenses()
    }

    suspend fun getExpensesByCategory(category: String): List<ExpenseEntity> {
        return expenseDao.getExpensesByCategory(category)
    }

    fun getTotalExpenses(): Flow<Double> {
        return expenseDao.getTotalExpenses()
    }

    suspend fun getExpensesBetweenDates(start: String, end: String): List<ExpenseEntity> {
        return expenseDao.getExpensesBetweenDates(start, end)
    }
}