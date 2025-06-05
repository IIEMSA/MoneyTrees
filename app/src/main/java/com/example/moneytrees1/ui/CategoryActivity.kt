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

    private val menuItems = listOf(
        MenuItem("Home", MainActivity::class.java),
        MenuItem("Dashboard", DashboardActivity::class.java),
        MenuItem("Profile", ProfileActivity::class.java),
        MenuItem("Add Expense", ExpenseActivity::class.java),
        MenuItem("Budget Planner", BudgetPlannerActivity::class.java),
        MenuItem("Expense History", ExpenseHistoryActivity::class.java),
        MenuItem("Achievements", AchievementsActivity::class.java),
        MenuItem("Leaderboard", LeaderboardActivity::class.java),
        MenuItem("Game", GameActivity::class.java),
        MenuItem("Add Category", CategoryActivity::class.java)
    )

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Session: load ONLY from SharedPreferences
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val etCategoryName = findViewById<EditText>(R.id.et_category_name)
        val etCategoryAmount = findViewById<EditText>(R.id.et_category_amount)
        val btnSaveCategory = findViewById<Button>(R.id.btn_save_category)

        btnSaveCategory.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val amountString = etCategoryAmount.text.toString().trim()

            if (name.isNotBlank() && amountString.isNotBlank()) {
                val amount = amountString.toDoubleOrNull()
                if (amount == null) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(applicationContext)
                        if (db.categoryDao().getCategoryByName(userId, name) != null) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@CategoryActivity,
                                    "Category already exists!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@launch
                        }
                        db.categoryDao().insert(CategoryEntity(userId = userId, name = name, amount = amount))
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
            } else {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
            }
        }

        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        findViewById<ImageView>(R.id.nav_menu).setOnClickListener {
            showSideMenu()
        }
    }

    private fun showSideMenu() {
        AlertDialog.Builder(this)
            .setTitle("Menu Options")
            .setItems(menuItems.map { it.title }.toTypedArray()) { _, which ->
                val intent = Intent(this, menuItems[which].targetActivity)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class MenuItem(val title: String, val targetActivity: Class<*>)
}