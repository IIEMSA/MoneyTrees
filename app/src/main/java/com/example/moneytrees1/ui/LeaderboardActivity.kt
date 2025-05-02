package com.example.moneytrees1.ui

import android.content.Intent
import com.example.moneytrees1.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.moneytrees1.ui.MainActivity.MenuItem


class LeaderboardActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_leaderboard)
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
