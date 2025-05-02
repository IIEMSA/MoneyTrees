package com.example.moneytrees1.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrees1.R
import com.example.moneytrees1.data.AppDatabase
import com.example.moneytrees1.data.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ExpenseHistoryActivity : AppCompatActivity() {

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

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryTotalAdapter: CategoryTotalAdapter
    private lateinit var totalTransactionsText: TextView
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val zarFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        maximumFractionDigits = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

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
        categoryTotalAdapter = CategoryTotalAdapter(zarFormat) // Initialize class property

        findViewById<RecyclerView>(R.id.rv_expenses).apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(this@ExpenseHistoryActivity)
        }

        findViewById<RecyclerView>(R.id.rv_category_totals).apply {
            adapter = categoryTotalAdapter // Use class property
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

        // In your date picker setup
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
        CoroutineScope(Dispatchers.IO).launch {
            val expenses = db.expenseDao().getAllExpenses()
            updateUI(expenses)
        }
    }

    private fun loadFilteredData() {
        val startDate = findViewById<EditText>(R.id.et_start_date).text.toString()
        val endDate = findViewById<EditText>(R.id.et_end_date).text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filteredExpenses = if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                    db.expenseDao().getExpensesBetweenDates(startDate, endDate)
                } else {
                    db.expenseDao().getAllExpenses()
                }

                updateUI(filteredExpenses)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExpenseHistoryActivity,
                        "Error loading expenses: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUI(expenses: List<ExpenseEntity>) {
        runOnUiThread {
            try {
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

            } catch (e: Exception) {
                Toast.makeText(
                    this@ExpenseHistoryActivity,
                    "Error updating UI: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                startActivity(Intent(this, menuItems[which].targetActivity))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class MenuItem(val title: String, val targetActivity: Class<*>)
}
