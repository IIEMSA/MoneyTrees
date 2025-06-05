package com.example.moneytrees1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val budgetType: String,
    val budgetAmount: Double,
    val groceriesAmount: Double,
    val transportAmount: Double,
    val entertainmentAmount: Double,
    val minimumGoal: Double,
    val maximumGoal: Double,
    val timestamp: Date = Date(),
    val userId: Int // Add this field
)
