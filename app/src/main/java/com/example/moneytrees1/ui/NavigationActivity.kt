package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.moneytrees1.R

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets the layout for the activity
        setContentView(R.layout.activity_navigation)

        // Find buttons by their ID from the layout
        val button3 = findViewById<Button>(R.id.button3) // Budget Planner
        val button4 = findViewById<Button>(R.id.button4) // Dashboard
        val button5 = findViewById<Button>(R.id.button5) // Transaction History
        val button6 = findViewById<Button>(R.id.button6) // Leaderboard
        val button7 = findViewById<Button>(R.id.button7) // Achievements
        val button8 = findViewById<Button>(R.id.button8) // Game
        val button9 = findViewById<Button>(R.id.button9) // Settings

        // Set click listeners to navigate to each corresponding activity
        button3.setOnClickListener {
            startActivity(Intent(this, BudgetPlannerActivity::class.java))
        }

        button4.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        button5.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        button6.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        button7.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        button8.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        button9.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
