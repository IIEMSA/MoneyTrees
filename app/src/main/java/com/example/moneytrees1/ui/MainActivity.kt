package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.databinding.ActivityMainBinding
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory
import com.example.moneytrees1.viewmodels.ExpenseViewModel
import com.example.moneytrees1.viewmodels.ExpenseViewModelFactory
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

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

    private lateinit var binding: ActivityMainBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var recentAdapter: ExpenseAdapter
    private var currentBudget: Budget? = null
    private var userId: Int = -1
    private var username: String = "User"

    companion object {
        private const val TAG = "MainActivity"

        private fun formatCurrency(amount: Double): String {
            val zarFormat = NumberFormat.getNumberInstance(Locale("en", "ZA")).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 2
            }
            return "R${zarFormat.format(amount)}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        // SESSION: Load only from SharedPreferences, ignore all intent extras
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = prefs.getInt("USER_ID", -1)
        username = prefs.getString("USERNAME", "User") ?: "User"
        Log.d(TAG, "MainActivity created. Loaded userId = $userId, username = $username")

        if (userId == -1) {
            Log.e(TAG, "User ID invalid or missing")
            Toast.makeText(this, "Invalid user session. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViewModels()
        setupUI(username)
        setupRecyclerView()
        setupObservers()
        setupNavigationListeners()
    }

    private fun initViewModels() {
        val app = application as MyApplication
        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(app.expenseRepository, userId)
        )[ExpenseViewModel::class.java]

        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository, userId)
        )[BudgetViewModel::class.java]

        Log.d(TAG, "ViewModels initialized with userId = $userId")
    }

    private fun setupRecyclerView() {
        recentAdapter = ExpenseAdapter(NumberFormat.getCurrencyInstance(Locale("en", "ZA")))
        binding.recentActivityRecycler.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        Log.d(TAG, "RecyclerView setup completed")
    }

    private fun setupUI(username: String) {
        binding.tvWelcome.text = "Hello, $username!"
        Log.d(TAG, "UI setup completed")
    }

    private fun setupObservers() {
        // Observe recent expenses
        expenseViewModel.recentExpenses.observe(this) { expenses ->
            if (!expenses.isNullOrEmpty()) {
                recentAdapter.submitList(expenses)
                binding.emptyRecentActivity.visibility = View.GONE
                Log.d(TAG, "Recent expenses loaded: ${expenses.size}")
            } else {
                recentAdapter.submitList(emptyList())
                binding.emptyRecentActivity.visibility = View.VISIBLE
                Log.d(TAG, "No recent expenses for user $userId")
            }
        }

        // Observe total expenses
        expenseViewModel.totalExpenses.observe(this) { total ->
            binding.tvTotalExpenses.text = "Total Expenses: ${formatCurrency(total ?: 0.0)}"
            updateRemainingBudget(total ?: 0.0)
            updateGoalBar()
        }

        // Observe current budget
        budgetViewModel.currentBudget.observe(this) { budget ->
            currentBudget = budget
            if (budget == null) {
                Log.w(TAG, "No budget for userId $userId")
                updateBudgetDisplay(null)
                binding.goalBarWrapper.visibility = View.GONE
                binding.tvBudgetSpent.text = ""
                binding.tvBudgetRemaining.text = ""
                Toast.makeText(this, "No budget for this user. Please create one!", Toast.LENGTH_SHORT).show()
            } else {
                updateBudgetDisplay(budget)
                updateGoalBar()
                binding.goalBarWrapper.visibility = View.VISIBLE
            }
        }

        // Observe total spent
        budgetViewModel.totalSpent.observe(this) { spent ->
            binding.tvBudgetSpent.text = "Spent: ${formatCurrency(spent ?: 0.0)}"
            updateRemainingBudget(spent ?: 0.0)
            updateGoalBar()
        }

        Log.d(TAG, "Observers set up")
    }

    private fun updateGoalBar() {
        lifecycleScope.launch {
            try {
                val budget = currentBudget ?: return@launch // Don't update if no budget!
                val minGoal = budget.minimumGoal
                val maxGoal = budget.maximumGoal.takeIf { it > 0 } ?: return@launch

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val endDate = sdf.format(calendar.time)

                calendar.add(Calendar.MONTH, -1)
                val startDate = sdf.format(calendar.time)

                val expenses = withContext(Dispatchers.IO) {
                    expenseViewModel.getExpensesBetweenDates(startDate, endDate)
                }

                val totalSpent = expenses.sumOf { it.amount }

                if (!isViewAvailable(binding.goalBarWrapper)) return@launch

                binding.goalBarWrapper.post {
                    try {
                        val barWidth = binding.goalBarWrapper.width.toFloat().takeIf { it > 0 } ?: return@post

                        // Update labels
                        binding.tvGoalMin.text = "Min: ${formatCurrency(minGoal)}"
                        binding.tvGoalCurrent.text = "You: ${formatCurrency(totalSpent)}"
                        binding.tvGoalMax.text = "Max: ${formatCurrency(maxGoal)}"

                        // Calculate positions
                        val minPosition = (minGoal / maxGoal).coerceAtMost(1.0) * barWidth
                        val currentPosition = (totalSpent / maxGoal).coerceAtMost(1.0) * barWidth

                        // Update progress fill
                        binding.goalProgressFill.layoutParams = binding.goalProgressFill.layoutParams.apply {
                            width = currentPosition.toInt().coerceAtLeast(1)
                        }

                        // Update markers
                        (binding.minGoalMarker.layoutParams as? RelativeLayout.LayoutParams)?.apply {
                            marginStart = minPosition.toInt() - 1
                        }
                        binding.minGoalMarker.visibility = View.VISIBLE
                        binding.maxGoalMarker.visibility = View.VISIBLE

                        // Update color
                        val fillColor = when {
                            totalSpent < minGoal -> ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light)
                            totalSpent > maxGoal -> ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light)
                            else -> ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_dark)
                        }
                        binding.goalProgressFill.setBackgroundColor(fillColor)

                        // Position indicator
                        binding.currentIndicatorContainer.layoutParams =
                            (binding.currentIndicatorContainer.layoutParams as? RelativeLayout.LayoutParams)?.apply {
                                marginStart = (currentPosition - binding.currentIndicatorContainer.width / 2).toInt()
                                    .coerceIn(0, (barWidth - binding.currentIndicatorContainer.width).toInt())
                            } ?: return@post

                        Log.d(TAG, "Goal bar updated")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating goal bar", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateGoalBar", e)
            }
        }
    }

    private fun isViewAvailable(view: View): Boolean {
        return view.width > 0 && view.height > 0 && view.isAttachedToWindow
    }

    private fun updateBudgetDisplay(budget: Budget?) {
        if (budget != null) {
            binding.budgetStatus.text = "Budget Type\n${budget.budgetType}"
            binding.accountBalance.text = "Total Budget\n${formatCurrency(budget.budgetAmount)}"
            val dailyAverage = budget.budgetAmount / 30
            binding.savingsProgress.text = "Daily Average\n${formatCurrency(dailyAverage)}"
        } else {
            binding.budgetStatus.text = "No Budget Set"
            binding.accountBalance.text = "Total Budget\n${formatCurrency(0.0)}"
            binding.savingsProgress.text = "Daily Average\n${formatCurrency(0.0)}"
        }
    }

    private fun updateRemainingBudget(spent: Double) {
        currentBudget?.let { budget ->
            val remaining = budget.budgetAmount - spent
            binding.tvBudgetRemaining.text = "Remaining: ${formatCurrency(remaining)}"
            binding.budgetStatus.text = when {
                remaining <= 0 -> "Over Budget!"
                remaining < budget.budgetAmount * 0.25 -> "Low Funds"
                else -> "On Track"
            }
        } ?: run {
            binding.tvBudgetRemaining.text = ""
            // Don't update budgetStatus here because null budget already handled above
        }
    }

    private fun showBudgetSummary(budget: Budget) {
        try {
            val totalExpenses = expenseViewModel.totalExpenses.value ?: 0.0
            val plannedSpending = budgetViewModel.totalSpent.value ?: 0.0

            val message = """
                Budget Type: ${budget.budgetType}
                Total Budget: ${formatCurrency(budget.budgetAmount)}
                Planned Spending: ${formatCurrency(plannedSpending)}
                Actual Expenses: ${formatCurrency(totalExpenses)}
                Remaining: ${formatCurrency(budget.budgetAmount - totalExpenses)}
                
                Minimum Goal: ${formatCurrency(budget.minimumGoal)}
                Maximum Goal: ${formatCurrency(budget.maximumGoal)}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Budget Summary")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing budget summary", e)
            Toast.makeText(this, "Error showing budget details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoBudgetWarning() {
        Toast.makeText(this, "Please create a budget first!", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigationListeners() {
        binding.navMenu.setOnClickListener { showSideMenu() }
        // Add similar listeners for other buttons if needed—no extras needed!
    }

    private fun showSideMenu() {
        AlertDialog.Builder(this)
            .setTitle("Menu Options")
            .setItems(menuItems.map { it.title }.toTypedArray()) { _, which ->
                val targetActivity = menuItems[which].targetActivity
                startActivity(Intent(this, targetActivity))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class MenuItem(val title: String, val targetActivity: Class<*>)
}