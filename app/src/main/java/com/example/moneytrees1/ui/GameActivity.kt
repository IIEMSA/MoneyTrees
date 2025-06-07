package com.example.moneytrees1.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.moneytrees1.R
import com.example.moneytrees1.ui.MainActivity.MenuItem

class GameActivity : AppCompatActivity() {

    // 📋 Menu items for the side menu navigation
    private val menuItems = listOf(
        MainActivity.MenuItem("Home", MainActivity::class.java),
        MainActivity.MenuItem("Dashboard", DashboardActivity::class.java),
        MainActivity.MenuItem("Profile", ProfileActivity::class.java),
        MainActivity.MenuItem("Add Expense", ExpenseActivity::class.java),
        MainActivity.MenuItem("Budget Planner", BudgetPlannerActivity::class.java),
        MainActivity.MenuItem("Expense History", ExpenseHistoryActivity::class.java),
        MainActivity.MenuItem("Achievements", AchievementsActivity::class.java),
        MainActivity.MenuItem("Leaderboard", LeaderboardActivity::class.java),
        MainActivity.MenuItem("Game", GameActivity::class.java),
        MainActivity.MenuItem("Add Category", CategoryActivity::class.java)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    // Setup navigation menu items click listeners
    private fun setupNavigationListeners() {
        // ☰ Show side menu with options
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

    // 🗂 Data class for menu items
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}
