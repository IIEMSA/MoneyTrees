package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun getAllExpenses(userId: Int): List<ExpenseEntity>

    @Query("SELECT DISTINCT category FROM expenses WHERE userId = :userId")
    suspend fun getAllCategories(userId: Int): List<String>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    fun getTotalExpenses(userId: Int): Flow<Double>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :start AND :end")
    fun getExpensesBetweenDates(userId: Int, start: String, end: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC LIMIT 5")
    fun getRecentExpenses(userId: Int): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :start AND :end 
        AND category = :category 
        ORDER BY date ASC
    """)
    suspend fun getExpensesBetweenDatesAndCategory(
        userId: Int,
        start: String,
        end: String,
        category: String
    ): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category")
    suspend fun getExpensesByCategory(userId: Int, category: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getAllExpensesFlow(userId: Int): Flow<List<ExpenseEntity>>

    // Add this to clear expenses for a specific user
    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun clearUserExpenses(userId: Int)
}