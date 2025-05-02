package com.example.moneytrees1.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.databinding.ActivityMainBinding
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Side menu navigation options
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
    private lateinit var expenseAdapter: ExpenseAdapter
    private var currentBudget: Budget? = null
    private var userId: Int = -1
    private var username: String = ""

    companion object {
        private const val EXTRA_USER_ID = "EXTRA_USER_ID"
        private const val EXTRA_USERNAME = "EXTRA_USERNAME"

        fun start(context: Context, userId: Int, username: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_USERNAME, username)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        fun formatCurrency(amount: Double): String {
            val zarFormat = NumberFormat.getNumberInstance(Locale("en", "ZA")).apply {
                maximumFractionDigits = 2
            }
            return "R${zarFormat.format(amount)}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MyApplication
        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository)
        )[BudgetViewModel::class.java]

        userId = intent.getIntExtra(EXTRA_USER_ID, -1)

        setupUI()
        setupObservers()
        setupNavigationListeners()
    }

    private fun setupUI() {
        binding.btnAddCategory.setOnClickListener {
            currentBudget?.let { showBudgetSummary(it) } ?: showNoBudgetWarning()
        }
    }


    private fun setupObservers() {
        // Budget observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetViewModel.currentBudget.collect { budget ->
                    currentBudget = budget
                    updateBudgetDisplay(budget)
                }
            }
        }

        // Total expenses observer
        budgetViewModel.totalExpenses.observe(this) { total ->
            val totalExpenses = total ?: 0.0
            binding.tvTotalExpenses.text = "Total Expenses: ${formatCurrency(totalExpenses)}"
            updateRemainingBudget(totalExpenses)
        }

        // Progress percentage observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetViewModel.progressPercentage.collect { percentage ->
                    binding.budgetProgress.progress = percentage
                }
            }
        }

        // Total spent observer
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                budgetViewModel.totalSpent.collect { spent ->
                    binding.tvBudgetSpent.text = "Spent: ${formatCurrency(spent)}"
                    updateRemainingBudget(spent)
                }
            }
        }
    }

    private fun updateBudgetDisplay(budget: Budget?) {
        budget?.let {
            binding.budgetStatus.text = "Budget Type\n${it.budgetType}"
            binding.accountBalance.text = "Total Budget\n${formatCurrency(it.budgetAmount)}"

            val dailyAverage = it.budgetAmount / 30
            binding.savingsProgress.text = "Daily Average\n${formatCurrency(dailyAverage)}"
        } ?: run {
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

            val percentage = if (budget.budgetAmount > 0) {
                ((spent / budget.budgetAmount) * 100).coerceAtMost(100.0).toInt()
            } else 0
            binding.budgetProgress.progress = percentage
        }
    }

    private fun showBudgetSummary(budget: Budget) {
        val totalExpenses = budgetViewModel.totalExpenses.value ?: 0.0
        val message = """
            Budget Type: ${budget.budgetType}
            Total Budget: ${formatCurrency(budget.budgetAmount)}
            Planned Spending: ${formatCurrency(budgetViewModel.totalSpent.value ?: 0.0)}
            Actual Expenses: ${formatCurrency(totalExpenses)}
            Remaining: ${formatCurrency(budget.budgetAmount - totalExpenses)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Budget Summary")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()

    }

    private fun showNoBudgetWarning() {
        Toast.makeText(this, "Please create a budget first!", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigationListeners() {
        binding.navMenu.setOnClickListener { showSideMenu() }
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun enableEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    data class MenuItem(val title: String, val targetActivity: Class<*>)
}