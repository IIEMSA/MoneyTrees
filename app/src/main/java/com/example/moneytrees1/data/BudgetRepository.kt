package com.example.moneytrees1.data

import kotlinx.coroutines.flow.Flow
import java.util.*

class BudgetRepository(private val budgetDao: BudgetDao) {
    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    suspend fun deleteUserBudgets(userId: Int) = budgetDao.deleteUserBudgets(userId)

    fun getLatestBudgetFlow(userId: Int): Flow<Budget?> = budgetDao.getLatestBudgetFlow(userId)

    suspend fun getLatestBudget(userId: Int): Budget? = budgetDao.getLatestBudget(userId)

    suspend fun getLastMonthGoals(userId: Int): List<Budget> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val oneMonthAgo = calendar.time
        return budgetDao.getBudgetsSince(userId, oneMonthAgo)
    }

    // Add this to clear user-specific budgets
    suspend fun clearUserBudgets(userId: Int) {
        budgetDao.clearUserBudgets(userId)
    }
}