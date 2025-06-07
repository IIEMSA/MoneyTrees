package com.example.moneytrees1

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.moneytrees1.data.AppDatabase
import com.example.moneytrees1.data.UserRepository
import com.example.moneytrees1.data.BudgetRepository
import com.example.moneytrees1.data.CategoryRepository
import com.example.moneytrees1.data.ExpenseRepository
import com.example.moneytrees1.data.NotificationRepository

class MyApplication : Application() {
    companion object {
        private const val TAG = "MyApplication"
        lateinit var instance: MyApplication
            private set
    }

    // Database instance
    private lateinit var _database: AppDatabase
    val database: AppDatabase
        get() = _database

    // Repositories
    lateinit var userRepository: UserRepository
    lateinit var budgetRepository: BudgetRepository
    lateinit var categoryRepository: CategoryRepository
    lateinit var expenseRepository: ExpenseRepository
    lateinit var notificationRepository: NotificationRepository


    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("GLOBAL_CRASH", "Uncaught exception", e)
            // Send crash report or restart app
        }
            Log.d(TAG, "MyApplication onCreate")
            instance = this

            try {
                // Initialize database
                _database = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "money_trees_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                Log.d(TAG, "Database initialized successfully")

                // Initialize repositories
                userRepository = UserRepository(database.userDao())
                budgetRepository = BudgetRepository(database.budgetDao())
                categoryRepository = CategoryRepository(database.categoryDao())
                expenseRepository = ExpenseRepository(database.expenseDao())
                notificationRepository = NotificationRepository(database.notificationDao())

                Log.d(TAG, "Repositories initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing database or repositories", e)
                throw RuntimeException("Database initialization failed", e)
            }
        }
    }