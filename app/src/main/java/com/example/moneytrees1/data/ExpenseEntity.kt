package com.example.moneytrees1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val date: String,         // Should be in yyyy-MM-dd format
    val startTime: String,    // Consider using timestamp instead
    val endTime: String,      // Consider using timestamp instead
    val imagePath: String?    // Nullable field
)