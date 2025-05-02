package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT DISTINCT category FROM expenses")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getExpensesBetweenDates(startDate: String, endDate: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT 3")
    fun getRecentExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :start AND :end AND category = :category ORDER BY date ASC")
    suspend fun getExpensesBetweenDatesAndCategory(start: String, end: String, category: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE category = :category")
    suspend fun getExpensesByCategory(category: String): List<ExpenseEntity>
}
