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
        MainActivity.MenuItem("Save The Bunny Game", LeaderboardActivity::class.java),
        MainActivity.MenuItem("Add Category", CategoryActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🧑 Get the actual player name and score from intent
        val userName = intent.getStringExtra("USER_NAME") ?: "You 🌟"
        val userScore = intent.getIntExtra("USER_SCORE", 0)

        // 🏆 Demo leaderboard users
        val demoUsers = mutableListOf(
            LeaderboardUser("Lorelai Gilmore", 8700),
            LeaderboardUser("Monica Geller", 8500),
            LeaderboardUser("Miles Morales", 8300),
            LeaderboardUser("Pam Beesly", 8100),
            LeaderboardUser("Toni Childs", 7900),
            LeaderboardUser("Rory Gilmore", 7700),
            LeaderboardUser("Phil Dunphy", 7400),
            LeaderboardUser("Peter Parker", 7100),
            LeaderboardUser("Dwight Schrute", 6900)
        )

        // 🔁 Prevent duplicate user entry
        demoUsers.removeAll { it.name == userName }

        // ➕ Add real user score if valid
        if (userScore > 0) {
            demoUsers.add(LeaderboardUser(userName, userScore))
        }

        // 🔽 Sort from highest to lowest score
        val sortedUsers = demoUsers.sortedByDescending { it.score }

        // 🎯 Set adapter with highlighted real user
        recyclerView.adapter = LeaderboardAdapter(sortedUsers, userName)

        // ☰ Setup menu click
        setupNavigationListeners()
    }

    // ☰ Handle menu button click
    private fun setupNavigationListeners() {
        findViewById<ImageView>(R.id.nav_menu).setOnClickListener {
            showSideMenu()
        }
    }

    fun startGame(view: View) {
        // 🔁 Replace content with GameView
        val gameView = GameView(this)
        setContentView(gameView)
    }



    // 📋 Side menu
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


