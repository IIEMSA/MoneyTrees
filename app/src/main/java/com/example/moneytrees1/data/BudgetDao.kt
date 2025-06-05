package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteUserBudgets(userId: Int)

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestBudget(userId: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    fun getLatestBudgetFlow(userId: Int): Flow<Budget?>

    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId 
        AND timestamp >= :fromDate 
        ORDER BY timestamp ASC
    """)
    fun getBudgetsSince(userId: Int, fromDate: Date): List<Budget>

    // Add this to clear budgets for a specific user
    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun clearUserBudgets(userId: Int)
}