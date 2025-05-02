package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets ORDER BY id DESC LIMIT 1")
    suspend fun getLatestBudget(): Budget?


    @Query("SELECT * FROM budgets ORDER BY id DESC LIMIT 1")
    fun getLatestBudgetFlow(): Flow<Budget?>

}