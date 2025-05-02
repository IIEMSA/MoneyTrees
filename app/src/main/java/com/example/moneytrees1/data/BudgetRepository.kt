package com.example.moneytrees1.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    fun getLatestBudgetFlow(): Flow<Budget?> = budgetDao.getLatestBudgetFlow()
    suspend fun getLatestBudget(): Budget? = budgetDao.getLatestBudget()
}