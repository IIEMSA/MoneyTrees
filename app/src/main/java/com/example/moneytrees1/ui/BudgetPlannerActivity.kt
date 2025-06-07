package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
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
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetPlannerActivity : AppCompatActivity() {

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

    // 🛠 UI elements
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var totalBudgetText: TextView
    private lateinit var categoryContainer: LinearLayout

    // 🧮 ViewModel for budget operations
    private lateinit var budgetViewModel: BudgetViewModel

    // 👤 Current logged-in user ID
    private var userId: Int = -1

    // 📊 Firebase Analytics instance
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_planner)

        // 🔥 Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "BudgetPlannerActivity")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "BudgetPlannerActivity")
        })

        // 🔑 Retrieve userId from SharedPreferences (user session)
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🏭 Initialize ViewModel with repository and userId
        val app = application as MyApplication
        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository, userId)
        )[BudgetViewModel::class.java]

        // 🧩 Set up UI references and observers
        initializeViews()
        setupObservers()
        setupNavigation()

        // 📦 Load categories and update UI accordingly
        loadCategoriesAndUpdateUI()

        // 💾 Save budget button clicked
        findViewById<Button>(R.id.btnSaveBudget).setOnClickListener { saveBudget() }
        // 🗑 Clear/reset all budgets and categories
        findViewById<Button>(R.id.btnClearBudgets).setOnClickListener { confirmClearBudgets() }
    }

    private fun initializeViews() {
        // 🔍 Find views by ID
        progressBar = findViewById(R.id.budgetProgressBar)
        progressText = findViewById(R.id.progressPercentageText)
        totalBudgetText = findViewById(R.id.totalBudgetAmount)
        categoryContainer = findViewById(R.id.categoryContainer)
    }

    private fun setupObservers() {
        // 👀 Observe current budget and update UI accordingly
        budgetViewModel.currentBudget.observe(this) { budget ->
            if (budget == null) {
                clearAllInputs()
            } else {
                updateUI(budget)
            }
        }
        // 👀 Observe total spent and update total budget text
        budgetViewModel.totalSpent.observe(this) { total ->
            totalBudgetText.text = "R ${"%.2f".format(total)}"
        }
        // 👀 Observe progress percentage and update progress bar and text
        budgetViewModel.progressPercentage.observe(this) { percentage ->
            progressBar.progress = percentage
            progressText.text = "$percentage% spent"
        }
    }

    private fun loadCategoriesAndUpdateUI() {
        lifecycleScope.launch {
            try {
                // 📥 Fetch all categories from repository
                val categories = (application as MyApplication).categoryRepository.getAllCategories(userId)
                updateCategoryUI(categories)
                calculateAndUpdate()
            } catch (e: Exception) {
                Toast.makeText(this@BudgetPlannerActivity, "Error loading categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategoryUI(categories: List<CategoryEntity>) {
        categoryContainer.removeAllViews() // 🧹 Clear existing views first
        var total = 0.0

        // 🔄 For each unique category, add dynamic views
        categories
            .groupBy { it.name }
            .values
            .map { it.first() }
            .forEach { category ->
                total += addDynamicCategoryView(category)
            }

        // 💰 Update total budget text with sum of categories
        totalBudgetText.text = "R ${"%.2f".format(total)}"
    }

    private fun addDynamicCategoryView(category: CategoryEntity): Double {
        // 🏗 Inflate a dynamic view for each category
        val view = LayoutInflater.from(this).inflate(R.layout.item_category, null).apply {
            findViewById<TextView>(R.id.categoryTitle).text = category.name

            val amountEt = findViewById<EditText>(R.id.categoryAmount)
            amountEt.setText("%.2f".format(category.amount))

            // ✍️ Listen for text changes to update category amount in DB and UI
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newAmt = s.toString().toDoubleOrNull() ?: 0.0
                    lifecycleScope.launch {
                        (application as MyApplication).categoryRepository.updateCategory(
                            category.copy(amount = newAmt)
                        )
                    }
                    calculateAndUpdate() // 🔄 Recalculate totals and update UI
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
            amountEt.addTextChangedListener(watcher)

            // 🗑 Show delete button and confirm delete on click
            findViewById<ImageView>(R.id.deleteCategory).apply {
                visibility = View.VISIBLE
                setOnClickListener { confirmDeleteCategory(category, this@apply.parent as View) }
            }
        }

        // ➕ Add the category view to the container
        categoryContainer.addView(view)

        // ↩️ Return amount for total calculation
        return view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun confirmDeleteCategory(category: CategoryEntity, view: View) {
        // ⚠️ Show confirmation dialog before deleting category
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
                    // 🗑 Delete main category and duplicates by name
                    repo.deleteCategory(category)
                    repo.getCategoriesByName(userId, category.name)
                        .drop(1)
                        .forEach { repo.deleteCategory(it) }
                }
                runOnUiThread {
                    categoryContainer.removeView(view) // 🧹 Remove category view from UI
                    calculateAndUpdate() // 🔄 Recalculate totals
                    Toast.makeText(this@BudgetPlannerActivity, "${category.name} deleted", Toast.LENGTH_SHORT).show()

                    // 📊 Log Firebase Analytics event for category deletion
                    logCustomEvent("category_deleted", Bundle().apply {
                        putString("category_name", category.name)
                    })
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@BudgetPlannerActivity, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmClearBudgets() {
        // ⚠️ Confirmation dialog before clearing all budgets and categories
        AlertDialog.Builder(this)
            .setTitle("Reset Everything")
            .setMessage("This will clear all budgets and categories. Continue?")
            .setPositiveButton("Reset All") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            // 🧹 Clear budgets and categories from database
                            (application as MyApplication).budgetRepository.clearUserBudgets(userId)
                            val categoryRepo = (application as MyApplication).categoryRepository
                            categoryRepo.getAllCategories(userId).forEach { categoryRepo.deleteCategory(it) }
                        }
                        runOnUiThread {
                            // 🧽 Clear all input fields and reset UI elements
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

                            // 📊 Log Firebase event for clearing budgets
                            logCustomEvent("budgets_cleared")
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
        // 🔢 Sum all category amounts
        var total = 0.0
        (0 until categoryContainer.childCount).forEach { i ->
            val view = categoryContainer.getChildAt(i)
            total += view.findViewById<EditText>(R.id.categoryAmount).text.toString().toDoubleOrNull() ?: 0.0
        }

        // 💵 Get budget amount input
        val budgetAmount = findViewById<EditText>(R.id.budgetAmount).text.toString().toDoubleOrNull() ?: 0.0

        // 💰 Update total spent display
        totalBudgetText.text = "R ${"%.2f".format(total)}"

        // 📊 Update progress bar and percentage text
        if (budgetAmount > 0) {
            val percentage = ((total / budgetAmount) * 100).coerceAtMost(100.0).toInt()
            progressBar.progress = percentage
            progressText.text = "$percentage% spent"
        }
    }

    private fun clearAllInputs() {
        // 🧼 Clear all budget input fields and UI progress indicators
        findViewById<EditText>(R.id.budgetAmount).text?.clear()
        findViewById<EditText>(R.id.minimumGoal).text?.clear()
        findViewById<EditText>(R.id.maximumGoal).text?.clear()
        categoryContainer.removeAllViews()
        totalBudgetText.text = "R 0.00"
        progressBar.progress = 0
        progressText.text = "0%"
    }

    private fun updateUI(budget: Budget) {
        // 📝 Update UI inputs with current budget values
        findViewById<EditText>(R.id.budgetAmount).setText(budget.budgetAmount.toString())
        findViewById<EditText>(R.id.minimumGoal).setText(budget.minimumGoal.toString())
        findViewById<EditText>(R.id.maximumGoal).setText(budget.maximumGoal.toString())
        findViewById<RadioButton>(R.id.btnMonthly).isChecked = budget.budgetType == "Monthly"
        findViewById<RadioButton>(R.id.btnWeekly).isChecked = budget.budgetType == "Weekly"
    }

    private fun saveBudget() {
        lifecycleScope.launch {
            try {
                // 🗂 Gather all category amounts into a map
                val categoryValues = mutableMapOf<String, Double>().apply {
                    (0 until categoryContainer.childCount).forEach { i ->
                        val view = categoryContainer.getChildAt(i)
                        val name = view.findViewById<TextView>(R.id.categoryTitle).text.toString()
                        val amount = view.findViewById<EditText>(R.id.categoryAmount)
                            .text.toString().toDoubleOrNull() ?: 0.0
                        put(name, amount)
                    }
                }

                // 📊 Create Budget object from inputs
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

                // 💾 Save budget and categories to DB in background
                withContext(Dispatchers.IO) {
                    budgetViewModel.insertBudget(budget)
                    saveDynamicCategories(categoryValues)
                }

                // ✅ Notify user and refresh UI on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BudgetPlannerActivity, "Budget saved!", Toast.LENGTH_SHORT).show()

                    // 📊 Log Firebase Analytics event for budget save
                    logCustomEvent("budget_saved", Bundle().apply {
                        putDouble("budget_amount", budget.budgetAmount)
                        putString("budget_type", budget.budgetType)
                    })

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
                // 🔍 Check if category exists; update or insert accordingly
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
        // ➕ Navigate to Add Category screen
        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            logCustomEvent("navigate_add_category")
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
        }

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

    private fun logCustomEvent(eventName: String, params: Bundle? = null) {
        // 📈 Log custom events to Firebase Analytics
        firebaseAnalytics.logEvent(eventName, params)
        android.util.Log.d("FirebaseAnalytics", "Logged event: $eventName")
    }

    // 🗂 Data class for menu items
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}
