package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrees1.R
import com.example.moneytrees1.data.LeaderboardUser

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

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
        setContentView(R.layout.activity_leaderboard)

        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val realUserName = "You 🌟"

        val demoUsers = listOf(
            LeaderboardUser("Lorelai Gilmore", 8700),
            LeaderboardUser("Monica Geller", 8500),
            LeaderboardUser("Miles Morales", 8300),
            LeaderboardUser("Pam Beesly", 8100),
            LeaderboardUser("Toni Childs", 7900),
            LeaderboardUser("Rory Gilmore", 7700),
            LeaderboardUser("Phil Dunphy", 7400),
            LeaderboardUser("Peter Parker", 7100),
            LeaderboardUser("Dwight Schrute", 6900),
            LeaderboardUser(realUserName, 7200)
        ).sortedByDescending { it.score }

        recyclerView.adapter = LeaderboardAdapter(demoUsers, realUserName)
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
