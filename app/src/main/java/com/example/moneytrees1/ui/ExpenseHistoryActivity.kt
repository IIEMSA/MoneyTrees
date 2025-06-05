package com.example.moneytrees1.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.data.ExpenseEntity
import com.example.moneytrees1.viewmodels.ExpenseViewModel
import com.example.moneytrees1.viewmodels.ExpenseViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ExpenseHistoryActivity : AppCompatActivity() {

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

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryTotalAdapter: CategoryTotalAdapter
    private lateinit var totalTransactionsText: TextView
    private val zarFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        maximumFractionDigits = 2
    }
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        // SESSION: Get userId ONLY from SharedPreferences
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize ViewModel
        val app = application as MyApplication
        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(app.expenseRepository, userId)
        )[ExpenseViewModel::class.java]

        totalTransactionsText = findViewById(R.id.tv_total_transactions)
        setupRecyclerViews()
        setupDatePickers()
        loadAllData()

        findViewById<Button>(R.id.btn_filter).setOnClickListener {
            loadFilteredData()
        }
        setupNavigationListeners()
    }

    private fun setupRecyclerViews() {
        expenseAdapter = ExpenseAdapter(zarFormat)
        categoryTotalAdapter = CategoryTotalAdapter(zarFormat)
        findViewById<RecyclerView>(R.id.rv_expenses).apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(this@ExpenseHistoryActivity)
        }
        findViewById<RecyclerView>(R.id.rv_category_totals).apply {
            adapter = categoryTotalAdapter
            layoutManager = LinearLayoutManager(
                this@ExpenseHistoryActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupDatePickers() {
        val startDateEt = findViewById<EditText>(R.id.et_start_date)
        val endDateEt = findViewById<EditText>(R.id.et_end_date)
        val calendar = Calendar.getInstance()
        startDateEt.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                startDateEt.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        endDateEt.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                endDateEt.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadAllData() {
        expenseViewModel.allExpenses.observe(this) { expenses ->
            updateUI(expenses)
        }
    }

    private fun loadFilteredData() {
        val startDate = findViewById<EditText>(R.id.et_start_date).text.toString()
        val endDate = findViewById<EditText>(R.id.et_end_date).text.toString()

        lifecycleScope.launch {
            val filteredExpenses = expenseViewModel.getExpensesBetweenDates(startDate, endDate)
            updateUI(filteredExpenses)
        }
    }

    private fun updateUI(expenses: List<ExpenseEntity>) {
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

        expenseAdapter.submitList(expenses)
        categoryTotalAdapter.updateData(categoryTotals)

        totalTransactionsText.text = getString(
            R.string.total_transactions,
            zarFormat.format(expenses.sumOf { it.amount })
        )

        findViewById<TextView>(R.id.empty_state_text).visibility =
            if (expenses.isEmpty()) View.VISIBLE else View.GONE

        findViewById<RecyclerView>(R.id.rv_category_totals).visibility =
            if (categoryTotals.isNotEmpty()) View.VISIBLE else View.GONE
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