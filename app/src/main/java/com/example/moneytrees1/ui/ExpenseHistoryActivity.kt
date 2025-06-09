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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

// Activity displaying expense history list, loading data from Firebase and local ViewModel
class ExpenseHistoryActivity : AppCompatActivity() {

    // Menu options for navigation drawer / menu
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

    private lateinit var expenseViewModel: ExpenseViewModel      // ViewModel for local expense data
    private lateinit var expenseAdapter: ExpenseAdapter          // RecyclerView adapter for expenses
    private lateinit var totalTransactionsText: TextView         // TextView showing total expenses
    private val zarFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).apply {
        maximumFractionDigits = 2 // Format currency with 2 decimals
    }
    private var userId: Int = -1                                 // Current logged in user ID

    private lateinit var databaseRef: DatabaseReference           // Firebase database reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_history)

        // Load userId from SharedPreferences session
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            // No valid user session - redirect to login
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize Firebase reference to the current user's expenses node
        databaseRef = Firebase.database.reference.child("users").child(userId.toString()).child("expenses")

        // Initialize ViewModel for local data access (optional but useful)
        val app = application as MyApplication
        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(app.expenseRepository, userId)
        )[ExpenseViewModel::class.java]

        totalTransactionsText = findViewById(R.id.tv_total_transactions)

        setupRecyclerViews()    // Setup RecyclerViews and adapters
        setupDatePickers()      // Setup date picker dialogs for filtering
        loadExpensesFromFirebase()  // Load expenses from Firebase on startup

        // Filter button listener to reload filtered expenses from local ViewModel
        findViewById<Button>(R.id.btn_filter).setOnClickListener {
            loadFilteredData()
        }

        setupNavigationListeners()  // Setup side menu navigation
    }

    // Setup RecyclerView and Adapter for displaying expenses
    private fun setupRecyclerViews() {
        expenseAdapter = ExpenseAdapter(zarFormat)
        findViewById<RecyclerView>(R.id.rv_expenses).apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(this@ExpenseHistoryActivity)
        }
    }

    // Setup click listeners on date EditTexts to show date pickers
    private fun setupDatePickers() {
        val startDateEt = findViewById<EditText>(R.id.et_start_date)
        val endDateEt = findViewById<EditText>(R.id.et_end_date)
        val calendar = Calendar.getInstance()

        // Show DatePickerDialog and set chosen date to startDate EditText
        startDateEt.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                startDateEt.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Show DatePickerDialog and set chosen date to endDate EditText
        endDateEt.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                endDateEt.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    // Load expenses from Firebase database for the current user
    private fun loadExpensesFromFirebase() {
        databaseRef.get().addOnSuccessListener { snapshot ->
            val expensesFromFirebase = mutableListOf<ExpenseEntity>()

            // Iterate each child (expense) and parse to ExpenseEntity
            snapshot.children.forEach { child ->
                val expense = child.getValue(ExpenseEntity::class.java)
                expense?.let { expensesFromFirebase.add(it) }
            }

            // Update UI on main thread
            runOnUiThread {
                updateUI(expensesFromFirebase)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load expenses from Firebase", Toast.LENGTH_SHORT).show()
        }
    }

    // Load filtered expenses from local ViewModel between given dates
    private fun loadFilteredData() {
        val startDate = findViewById<EditText>(R.id.et_start_date).text.toString()
        val endDate = findViewById<EditText>(R.id.et_end_date).text.toString()

        // Use coroutine to fetch filtered data asynchronously
        lifecycleScope.launch {
            val filteredExpenses = expenseViewModel.getExpensesBetweenDates(startDate, endDate)
            updateUI(filteredExpenses)
        }
    }

    // Update RecyclerView and total transaction TextView with given expenses
    private fun updateUI(expenses: List<ExpenseEntity>) {
        expenseAdapter.submitList(expenses) // Update list in adapter

        // Update total transaction amount text
        totalTransactionsText.text = getString(
            R.string.total_transactions,
            zarFormat.format(expenses.sumOf { it.amount })
        )

        // Show empty state text if no expenses
        findViewById<TextView>(R.id.empty_state_text).visibility =
            if (expenses.isEmpty()) View.VISIBLE else View.GONE
    }

    // Setup navigation menu button listener
    private fun setupNavigationListeners() {
        findViewById<ImageView>(R.id.nav_menu).setOnClickListener {
            showSideMenu()
        }
    }

    // Show side menu dialog with list of menu items
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

    // Save a new or updated expense to Firebase (call this when you add/edit an expense)
    private fun saveExpenseToFirebase(expense: ExpenseEntity) {
        val key = databaseRef.push().key ?: return // Generate unique key
        databaseRef.child(key).setValue(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
    }

    // Data class representing a menu item (title + activity class)
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}
