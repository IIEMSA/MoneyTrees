package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.moneytrees1.R
import com.example.moneytrees1.data.CategoryEntity
import com.example.moneytrees1.data.AppDatabase
import com.example.moneytrees1.ui.MainActivity.MenuItem

class CategoryActivity : AppCompatActivity() {

    // Side menu navigation options
    private val menuItems = listOf(
        com.example.moneytrees1.ui.MainActivity.MenuItem("Home", MainActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Dashboard",
            DashboardActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem("Profile", ProfileActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Add Expense",
            ExpenseActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Budget Planner",
            BudgetPlannerActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Expense History",
            ExpenseHistoryActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Achievements",
            AchievementsActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Leaderboard",
            LeaderboardActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem("Game", GameActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Add Category",
            CategoryActivity::class.java
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Initialize the UI elements
        val etCategoryName = findViewById<EditText>(R.id.et_category_name)
        val etCategoryAmount = findViewById<EditText>(R.id.et_category_amount)
        val btnSaveCategory = findViewById<Button>(R.id.btn_save_category)

        // Button click listener
        btnSaveCategory.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val amountString = etCategoryAmount.text.toString().trim()

            // Check if the category name and amount are valid
            if (name.isNotBlank() && amountString.isNotBlank()) {
                try {
                    val amount = amountString.toDouble() // Convert amount to a number

                    // Get the database instance
                    val db = AppDatabase.getDatabase(applicationContext)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val category = CategoryEntity(name = name, amount = amount)
                            db.categoryDao().insert(category)

                            // 🔍 Optional: Fetch and log all categories
                            val allCategories = db.categoryDao().getAllCategories()
                            allCategories.forEach {
                                println("Saved category: ${it.name} - Amount: ${it.amount}")
                            }

                            runOnUiThread {
                                Toast.makeText(
                                    this@CategoryActivity,
                                    "Category added successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@CategoryActivity,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                } catch (e: NumberFormatException) {
                    // Show error if the amount is not a valid number
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show error if category name or amount is blank
                Toast.makeText(this, "Please enter both category name and amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigationListeners() {
        // Side menu
        findViewById<ImageView>(R.id.nav_menu).setOnClickListener {
            showSideMenu()
        }
    }

    private fun showSideMenu() {
        AlertDialog.Builder(this)
            .setTitle("Menu Options")
            .setItems(menuItems.map { it.title }.toTypedArray()) { _, which ->
                startActivity(Intent(this, menuItems[which].targetActivity))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Simple data class for menu items
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}
