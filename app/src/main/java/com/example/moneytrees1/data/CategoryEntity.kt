package com.example.moneytrees1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    // This defines the primary key for the table, and Room will auto-generate unique values
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // This stores the name of the category (e.g., "Groceries", "Rent", etc.)
    val amount: Double // This stores the budgeted amount for the category (e.g., 500.00 for Rands)
)
