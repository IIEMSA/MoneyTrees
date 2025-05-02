package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.data.CategoryEntity
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
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

    private val viewModel: BudgetViewModel by lazy {
        ViewModelProvider(
            this,
            BudgetViewModelFactory(
                (application as MyApplication).budgetRepository,
                (application as MyApplication).expenseRepository
            )
        )[BudgetViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_planner)

        initializeViews()
        setupTextWatchers()
        setupObservers()
        setupNavigation()
        loadCategoriesAndUpdateUI()

        findViewById<Button>(R.id.btnSaveBudget).setOnClickListener {
            saveBudget()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategoriesAndUpdateUI()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.budgetProgressBar)
        progressText = findViewById(R.id.progressPercentageText)
        totalBudgetText = findViewById(R.id.totalBudgetAmount)
    }

    private fun updateCategoryUI(categories: List<CategoryEntity>) {
        val layout = findViewById<LinearLayout>(R.id.categoryContainer)
        layout.removeAllViews()
        var total = 0.0

        total += addCategoryView("Groceries", findViewById<EditText>(R.id.groceriesBudget).text.toString())
        total += addCategoryView("Transport", findViewById<EditText>(R.id.transportBudget).text.toString())
        total += addCategoryView("Entertainment", findViewById<EditText>(R.id.entertainmentBudget).text.toString())

        categories.forEach { category ->
            total += addCategoryView(category.name, "%.2f".format(category.amount))
        }

        totalBudgetText.text = "R ${"%.2f".format(total)}"
    }

    private fun addCategoryView(name: String, amount: String): Double {
        val view = LayoutInflater.from(this).inflate(R.layout.item_category, null).apply {
            findViewById<TextView>(R.id.categoryTitle).text = name
            findViewById<EditText>(R.id.categoryAmount).apply {
                setText(amount)
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) = calculateAndUpdate()
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
        }
        findViewById<LinearLayout>(R.id.categoryContainer).addView(view)
        return view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentBudget.collect { budget ->
                    budget?.let { updateUI(it) }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalSpent.collect { total ->
                    totalBudgetText.text = "R ${"%.2f".format(total)}"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progressPercentage.collect { percentage ->
                    progressBar.progress = percentage
                    progressText.text = "$percentage% spent"
                }
            }
        }
    }

    private fun loadCategoriesAndUpdateUI() {
        lifecycleScope.launch {
            val categories = (application as MyApplication).categoryRepository.getAllCategories()
            updateCategoryUI(categories)
        }
    }

    private fun updateUI(budget: Budget) {
        findViewById<EditText>(R.id.budgetAmount).setText(budget.budgetAmount.toString())
        findViewById<EditText>(R.id.groceriesBudget).setText(budget.groceriesAmount.toString())
        findViewById<EditText>(R.id.transportBudget).setText(budget.transportAmount.toString())
        findViewById<EditText>(R.id.entertainmentBudget).setText(budget.entertainmentAmount.toString())
        findViewById<EditText>(R.id.minimumGoal).setText(budget.minimumGoal.toString())
        findViewById<EditText>(R.id.maximumGoal).setText(budget.maximumGoal.toString())

        findViewById<RadioButton>(R.id.btnMonthly).isChecked = budget.budgetType == "Monthly"
        findViewById<RadioButton>(R.id.btnWeekly).isChecked = budget.budgetType == "Weekly"
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateAndUpdate()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        listOf(
            R.id.groceriesBudget,
            R.id.transportBudget,
            R.id.entertainmentBudget,
            R.id.budgetAmount
        ).forEach { id ->
            findViewById<EditText>(id).addTextChangedListener(textWatcher)
        }
    }

    private fun calculateAndUpdate() {
        val container = findViewById<LinearLayout>(R.id.categoryContainer)
        var total = (0 until container.childCount)
            .sumOf { i ->
                container.getChildAt(i)
                    .findViewById<EditText>(R.id.categoryAmount)
                    .text.toString()
                    .toDoubleOrNull() ?: 0.0
            }

        val budgetAmount = findViewById<EditText>(R.id.budgetAmount).text.toString().toDoubleOrNull() ?: 0.0
        totalBudgetText.text = "R ${"%.2f".format(total)}"

        if (budgetAmount > 0) {
            val percentage = ((total / budgetAmount) * 100).coerceAtMost(100.0).toInt()
            progressBar.progress = percentage
            progressText.text = "$percentage% spent"
        }
    }

    private fun saveBudget() {
        lifecycleScope.launch {
            try {
                // Save dynamic categories in IO context
                withContext(Dispatchers.IO) {
                    saveDynamicCategories()
                }

                // Create budget object
                val budget = Budget(
                    budgetType = if (findViewById<RadioButton>(R.id.btnMonthly).isChecked) "Monthly" else "Weekly",
                    budgetAmount = findViewById<EditText>(R.id.budgetAmount).text.toString().toDoubleOrNull() ?: 0.0,
                    groceriesAmount = findViewById<EditText>(R.id.groceriesBudget).text.toString().toDoubleOrNull() ?: 0.0,
                    transportAmount = findViewById<EditText>(R.id.transportBudget).text.toString().toDoubleOrNull() ?: 0.0,
                    entertainmentAmount = findViewById<EditText>(R.id.entertainmentBudget).text.toString().toDoubleOrNull() ?: 0.0,
                    minimumGoal = findViewById<EditText>(R.id.minimumGoal).text.toString().toDoubleOrNull() ?: 0.0,
                    maximumGoal = findViewById<EditText>(R.id.maximumGoal).text.toString().toDoubleOrNull() ?: 0.0
                )

                // Save budget in IO context
                withContext(Dispatchers.IO) {
                    viewModel.saveBudget(budget)
                }

                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetPlannerActivity, "Budget saved!", Toast.LENGTH_SHORT).show()
                    loadCategoriesAndUpdateUI()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetPlannerActivity, "Error saving budget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveDynamicCategories() {
        val container = findViewById<LinearLayout>(R.id.categoryContainer)
        val repo = (application as MyApplication).categoryRepository

        (0 until container.childCount).forEach { i ->
            val view = container.getChildAt(i)
            val name = view.findViewById<TextView>(R.id.categoryTitle).text.toString()
            val amount = view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0

            if (name !in listOf("Groceries", "Transport", "Entertainment")) {
                repo.insert(CategoryEntity(name = name, amount = amount))
            }
        }
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

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