package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.data.CategoryEntity
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetPlannerActivity : AppCompatActivity() {

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

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var totalBudgetText: TextView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var budgetViewModel: BudgetViewModel
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_planner)

        // SESSION: Get userId ONLY from SharedPreferences!
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val app = application as MyApplication
        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository, userId)
        )[BudgetViewModel::class.java]

        initializeViews()
        setupObservers()
        setupNavigation()
        loadCategoriesAndUpdateUI()

        findViewById<Button>(R.id.btnSaveBudget).setOnClickListener { saveBudget() }
        findViewById<Button>(R.id.btnClearBudgets).setOnClickListener { confirmClearBudgets() }
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.budgetProgressBar)
        progressText = findViewById(R.id.progressPercentageText)
        totalBudgetText = findViewById(R.id.totalBudgetAmount)
        categoryContainer = findViewById(R.id.categoryContainer)
    }

    private fun setupObservers() {
        budgetViewModel.currentBudget.observe(this) { budget ->
            if (budget == null) {
                clearAllInputs()
            } else {
                updateUI(budget)
            }
        }
        budgetViewModel.totalSpent.observe(this) { total ->
            totalBudgetText.text = "R ${"%.2f".format(total)}"
        }
        budgetViewModel.progressPercentage.observe(this) { percentage ->
            progressBar.progress = percentage
            progressText.text = "$percentage% spent"
        }
    }

    private fun loadCategoriesAndUpdateUI() {
        lifecycleScope.launch {
            try {
                val categories = (application as MyApplication).categoryRepository.getAllCategories(userId)
                updateCategoryUI(categories)
                calculateAndUpdate()
            } catch (e: Exception) {
                Toast.makeText(this@BudgetPlannerActivity, "Error loading categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategoryUI(categories: List<CategoryEntity>) {
        categoryContainer.removeAllViews()
        var total = 0.0
        categories
            .groupBy { it.name }
            .values
            .map { it.first() }
            .forEach { category ->
                total += addDynamicCategoryView(category)
            }
        totalBudgetText.text = "R ${"%.2f".format(total)}"
    }

    private fun addDynamicCategoryView(category: CategoryEntity): Double {
        val view = LayoutInflater.from(this).inflate(R.layout.item_category, null).apply {
            findViewById<TextView>(R.id.categoryTitle).text = category.name
            val amountEt = findViewById<EditText>(R.id.categoryAmount)
            amountEt.setText("%.2f".format(category.amount))
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newAmt = s.toString().toDoubleOrNull() ?: 0.0
                    lifecycleScope.launch {
                        (application as MyApplication).categoryRepository.updateCategory(
                            category.copy(amount = newAmt)
                        )
                    }
                    calculateAndUpdate()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
            amountEt.addTextChangedListener(watcher)
            findViewById<ImageView>(R.id.deleteCategory).apply {
                visibility = View.VISIBLE
                setOnClickListener { confirmDeleteCategory(category, this@apply.parent as View) }
            }
        }
        categoryContainer.addView(view)
        return view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun confirmDeleteCategory(category: CategoryEntity, view: View) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete ${category.name}?")
            .setPositiveButton("Delete") { _, _ -> deleteCategory(category, view) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: CategoryEntity, view: View) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val repo = (application as MyApplication).categoryRepository
                    repo.deleteCategory(category)
                    repo.getCategoriesByName(userId, category.name)
                        .drop(1)
                        .forEach { repo.deleteCategory(it) }
                }
                runOnUiThread {
                    categoryContainer.removeView(view)
                    calculateAndUpdate()
                    Toast.makeText(this@BudgetPlannerActivity, "${category.name} deleted", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@BudgetPlannerActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmClearBudgets() {
        AlertDialog.Builder(this)
            .setTitle("Reset Everything")
            .setMessage("This will clear all budgets and categories. Continue?")
            .setPositiveButton("Reset All") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            (application as MyApplication).budgetRepository.clearUserBudgets(userId)
                            val categoryRepo = (application as MyApplication).categoryRepository
                            categoryRepo.getAllCategories(userId).forEach { categoryRepo.deleteCategory(it) }
                        }
                        runOnUiThread {
                            findViewById<EditText>(R.id.budgetAmount).text?.clear()
                            findViewById<EditText>(R.id.minimumGoal).text?.clear()
                            findViewById<EditText>(R.id.maximumGoal).text?.clear()
                            findViewById<RadioButton>(R.id.btnMonthly).isChecked = true
                            progressBar.progress = 0
                            progressText.text = "0%"
                            totalBudgetText.text = "R 0.00"
                            loadCategoriesAndUpdateUI()
                            Toast.makeText(
                                this@BudgetPlannerActivity,
                                "All data reset to default",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@BudgetPlannerActivity,
                                "Reset failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun calculateAndUpdate() {
        var total = 0.0
        (0 until categoryContainer.childCount).forEach { i ->
            val view = categoryContainer.getChildAt(i)
            total += view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0
        }
        val budgetAmount = findViewById<EditText>(R.id.budgetAmount).text.toString().toDoubleOrNull() ?: 0.0
        totalBudgetText.text = "R ${"%.2f".format(total)}"

        if (budgetAmount > 0) {
            val percentage = ((total / budgetAmount) * 100).coerceAtMost(100.0).toInt()
            progressBar.progress = percentage
            progressText.text = "$percentage% spent"
        }
    }

    private fun clearAllInputs() {
        findViewById<EditText>(R.id.budgetAmount).text?.clear()
        findViewById<EditText>(R.id.minimumGoal).text?.clear()
        findViewById<EditText>(R.id.maximumGoal).text?.clear()
        categoryContainer.removeAllViews()
        totalBudgetText.text = "R 0.00"
        progressBar.progress = 0
        progressText.text = "0%"
    }

    private fun updateUI(budget: Budget) {
        findViewById<EditText>(R.id.budgetAmount).setText(budget.budgetAmount.toString())
        findViewById<EditText>(R.id.minimumGoal).setText(budget.minimumGoal.toString())
        findViewById<EditText>(R.id.maximumGoal).setText(budget.maximumGoal.toString())
        findViewById<RadioButton>(R.id.btnMonthly).isChecked = budget.budgetType == "Monthly"
        findViewById<RadioButton>(R.id.btnWeekly).isChecked = budget.budgetType == "Weekly"
    }

    private fun saveBudget() {
        lifecycleScope.launch {
            try {
                val categoryValues = mutableMapOf<String, Double>().apply {
                    (0 until categoryContainer.childCount).forEach { i ->
                        val view = categoryContainer.getChildAt(i)
                        val name = view.findViewById<TextView>(R.id.categoryTitle).text.toString()
                        val amount = view.findViewById<EditText>(R.id.categoryAmount)
                            .text.toString().toDoubleOrNull() ?: 0.0
                        put(name, amount)
                    }
                }

                val budget = Budget(
                    budgetType = if (findViewById<RadioButton>(R.id.btnMonthly).isChecked) "Monthly" else "Weekly",
                    budgetAmount = findViewById<EditText>(R.id.budgetAmount).text.toString().toDoubleOrNull() ?: 0.0,
                    groceriesAmount = 0.0,
                    transportAmount = 0.0,
                    entertainmentAmount = 0.0,
                    minimumGoal = findViewById<EditText>(R.id.minimumGoal).text.toString().toDoubleOrNull() ?: 0.0,
                    maximumGoal = findViewById<EditText>(R.id.maximumGoal).text.toString().toDoubleOrNull() ?: 0.0,
                    userId = userId
                )

                withContext(Dispatchers.IO) {
                    budgetViewModel.insertBudget(budget)
                    saveDynamicCategories(categoryValues)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetPlannerActivity, "Budget saved!", Toast.LENGTH_SHORT).show()
                    loadCategoriesAndUpdateUI()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetPlannerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveDynamicCategories(categoryValues: Map<String, Double>) {
        val repo = (application as MyApplication).categoryRepository
        categoryValues.forEach { (name, amount) ->
            try {
                val existing = repo.getCategoriesByName(userId, name)
                if (existing.isNotEmpty()) {
                    val updated = existing.first().copy(amount = amount)
                    repo.updateCategory(updated)
                } else {
                    repo.insert(CategoryEntity(userId = userId, name = name, amount = amount))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@BudgetPlannerActivity,
                        "Error saving $name: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }
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