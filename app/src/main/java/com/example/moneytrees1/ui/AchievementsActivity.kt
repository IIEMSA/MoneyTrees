package com.example.moneytrees1.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.widget.ImageView
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.moneytrees1.R

class AchievementsActivity : AppCompatActivity() {

    // Map each achievement ImageView to its achievement key for SharedPreferences
    private val achievementMap by lazy {
        mapOf(
            findViewById<ImageView>(R.id.imageView2)  to "first_budget",
            findViewById<ImageView>(R.id.imageView3)  to "saving_starter",
            findViewById<ImageView>(R.id.imageView4)  to "on_track",
            findViewById<ImageView>(R.id.imageView10) to "debt_destroyer",
            findViewById<ImageView>(R.id.imageView12) to "expense_slayer",
            findViewById<ImageView>(R.id.imageView13) to "smart_spender",
            findViewById<ImageView>(R.id.imageView14) to "goal_getter",
            findViewById<ImageView>(R.id.imageView15) to "no_spend_champ",
            findViewById<ImageView>(R.id.imageView16) to "financial_freedom",
            findViewById<ImageView>(R.id.imageView17) to "master_budgeter"
        )
    }

    // Menu items for your navigation drawer/side menu
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

    private val prefs by lazy { getSharedPreferences("achievements", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        setupNavigationListeners()
        setupAchievements()
    }

    // Set greyscale/color for achievements
    private fun setupAchievements() {
        achievementMap.forEach { (imageView, key) ->
            val isUnlocked = prefs.getBoolean(key, false)
            updateAchievementAppearance(imageView, isUnlocked)
            // No dialog, since all info is on the images!
        }
    }

    // Unlock=full color, else greyscale
    private fun updateAchievementAppearance(imageView: ImageView, unlocked: Boolean) {
        if (unlocked) {
            imageView.clearColorFilter()
        } else {
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            imageView.colorFilter = ColorMatrixColorFilter(matrix)
        }
    }

    // --- Side menu code ---
    private fun setupNavigationListeners() {
        findViewById<ImageView>(R.id.nav_menu)?.setOnClickListener {
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

    data class MenuItem(val title: String, val targetActivity: Class<*>)

    companion object {
        // Call this from anywhere to unlock achievements
        fun unlockAchievement(context: Context, achievementKey: String) {
            val prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(achievementKey, true).apply()
        }
    }
}