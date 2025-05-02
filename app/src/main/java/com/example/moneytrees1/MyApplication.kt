package com.example.moneytrees1

import android.app.Application
import com.example.moneytrees1.data.*

class MyApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao()) // Ensure CategoryDao is defined
    }
}
