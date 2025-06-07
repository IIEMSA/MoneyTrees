package com.example.moneytrees1.ui

// 🔒 Permissions & Security
import android.Manifest
import android.content.pm.PackageManager

// 📅 Date/Time Pickers
import android.app.DatePickerDialog

// 🏗️ Android Framework
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*

// 🏛️ AndroidX Components
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

// 🔥 Firebase Services
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

// 🏠 App Components
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory

// 📊 Charting Library
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

// 🌀 Coroutines
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// ⏰ Java Utilities
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

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


    // UI components references
    private lateinit var chart: BarChart
    private lateinit var etStart: EditText
    private lateinit var etEnd: EditText
    private lateinit var btnRefresh: Button
    private lateinit var legendContainer: LinearLayout
    private lateinit var btnDownloadPdf: Button
    private lateinit var minMaxLegend: TextView

    // ViewModel for budgeting data
    private lateinit var budgetViewModel: BudgetViewModel

    // Date formatter to format date strings in yyyy-MM-dd
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Colors for categories in chart
    private val categoryColorIds = listOf(
        R.color.cat1, R.color.cat2, R.color.cat3, R.color.cat4, R.color.cat5,
        R.color.cat6, R.color.cat7, R.color.cat8, R.color.cat9, R.color.cat10,
        R.color.cat11, R.color.cat12, R.color.cat13, R.color.cat14, R.color.cat15,
        R.color.cat16, R.color.cat17, R.color.cat18, R.color.cat19, R.color.cat20
    )

    // User session id, default invalid value
    private var userId: Int = -1

    // Firebase Firestore instance for database operations
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase SDK
        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()

        // SESSION: Retrieve logged in user ID from SharedPreferences
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize ViewModel with repositories and userId
        val app = application as MyApplication
        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository, userId)
        )[BudgetViewModel::class.java]

        // Initialize UI references from layout
        chart = findViewById(R.id.barChart)
        etStart = findViewById(R.id.et_start_date)
        etEnd = findViewById(R.id.et_end_date)
        btnRefresh = findViewById(R.id.btnRefresh)
        legendContainer = findViewById(R.id.legendContainer)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)
        minMaxLegend = findViewById(R.id.minmax_legend)

        // Set default date range to current month: start = first day, end = today
        val now = Calendar.getInstance()
        now.set(Calendar.DAY_OF_MONTH, 1)
        etStart.setText(dateFormat.format(now.time))
        now.time = Date()
        etEnd.setText(dateFormat.format(now.time))

        // Set listeners for date inputs to show date picker dialog
        etStart.setOnClickListener { pickDateDialog(etStart) }
        etEnd.setOnClickListener { pickDateDialog(etEnd) }

        // Button to refresh chart data
        btnRefresh.setOnClickListener { loadAndPopulateChart() }

        // Button to export dashboard as PDF
        btnDownloadPdf.setOnClickListener { checkAndSavePdf() }

        // Setup navigation menu listeners (side menu)
        setupNavigationListeners()

        // Observe changes in categories or expenses and update chart accordingly
        lifecycleScope.launch {
            app.categoryRepository.getAllCategoriesFlow(userId).collect {
                loadAndPopulateChart()
            }
        }
        lifecycleScope.launch {
            app.expenseRepository.getAllExpensesFlow(userId).collect {
                loadAndPopulateChart()
            }
        }

        // Observe current budget changes to refresh chart goals
        budgetViewModel.currentBudget.observe(this, Observer { _ ->
            loadAndPopulateChart()
        })

        // --- Example Firebase Usage ---

        // Example: Log current user's document from Firestore (for debugging/demo)
        firestore.collection("users").document(userId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "No username"
                    Toast.makeText(this, "Welcome back, $username!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User data not found in Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Opens a date picker dialog and sets selected date into EditText
    private fun pickDateDialog(editText: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this, { _, y, m, d ->
                val str = String.format("%04d-%02d-%02d", y, m + 1, d)
                editText.setText(str)
                loadAndPopulateChart()  // Refresh chart on date change
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Load data and update chart and legend
    private fun loadAndPopulateChart() {
        lifecycleScope.launch {
            val start = etStart.text.toString()
            val end = etEnd.text.toString()
            val app = application as MyApplication

            // Load categories and expenses filtered by date range
            val categories = app.categoryRepository.getAllCategories(userId).map { it.name }.distinct()
            val expenses = app.expenseRepository.getAllExpenses(userId)
            val expensesByCat = categories.associateWith { cat ->
                expenses.filter { it.category == cat && it.date >= start && it.date <= end }
                    .sumOf { it.amount }
            }

            val budget = budgetViewModel.currentBudget.value
            val minGoal = budget?.minimumGoal ?: 0.0
            val maxGoal = budget?.maximumGoal ?: 0.0

            // Update chart with latest data
            updateGraph(categories, expensesByCat, minGoal, maxGoal)

            // Update the color-coded legend
            buildLegend(categories)
        }
    }

    // Helper to update BarChart with categories and their spending totals
    private fun updateGraph(
        categories: List<String>,
        expensesByCat: Map<String, Double>,
        minGoal: Double,
        maxGoal: Double
    ) {
        // Create BarEntry objects for each category expense
        val entries = categories.mapIndexed { i, cat ->
            BarEntry(i.toFloat(), expensesByCat[cat]?.toFloat() ?: 0f)
        }

        // Formatter for axis and values to show K/M for thousands/millions
        val thousandsFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000 -> "%.1fM".format(value / 1_000_000)
                    value >= 1_000 -> "%.1fK".format(value / 1_000)
                    else -> "%.0f".format(value)
                }
            }
        }

        // Configure BarDataSet with entries and colors
        val barDataSet = BarDataSet(entries, "Spending per Category").apply {
            setColors(getCategoryColors(entries.size))
            valueTextSize = 11f
            valueFormatter = thousandsFormatter
        }

        // Prepare BarData with BarDataSet
        val barData = BarData(barDataSet)
        barData.barWidth = 0.7f

        // Configure x-axis with category labels
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(categories)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 11f
        xAxis.labelRotationAngle = -45f

        // Configure left y-axis with limit lines for budget goals
        val leftAxis = chart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.valueFormatter = thousandsFormatter
        leftAxis.granularity = 500f
        leftAxis.axisMinimum = 0f

        val grey = ContextCompat.getColor(this, R.color.grey)

        val minLimit = LimitLine(minGoal.toFloat(), "Min Goal").apply {
            lineColor = grey
            lineWidth = 3f
            textColor = grey
            textSize = 11f
        }
        leftAxis.addLimitLine(minLimit)

        val maxLimit = LimitLine(maxGoal.toFloat(), "Max Goal").apply {
            lineColor = grey
            lineWidth = 3f
            textColor = grey
            textSize = 11f
        }
        leftAxis.addLimitLine(maxLimit)

        // Finalize chart configuration
        chart.data = barData
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(900)
        chart.invalidate()
    }

    // Returns list of colors cycling through predefined category colors
    private fun getCategoryColors(size: Int): List<Int> =
        (0 until size).map { i ->
            ContextCompat.getColor(this, categoryColorIds[i % categoryColorIds.size])
        }

    // Build and display legend for categories with colored squares
    private fun buildLegend(categories: List<String>) {
        legendContainer.removeAllViews()
        val colors = getCategoryColors(categories.size)
        for (i in categories.indices) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 4, 0, 4) }
                minimumHeight = 24
            }
            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(36, 26)
                setBackgroundColor(colors[i])
            }
            val label = TextView(this).apply {
                text = "  ${categories[i]}"
                textSize = 15f
                setPadding(6, 0, 0, 0)
            }
            row.addView(colorView)
            row.addView(label)
            legendContainer.addView(row)
        }
    }

    // Check storage permissions and save dashboard screenshot as PDF
    private fun checkAndSavePdf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1002
                )
                return
            }
        }
        saveDashboardAsPdf()
    }

    // Handle permissions result for storage access
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveDashboardAsPdf()
        } else {
            Toast.makeText(this, "Permission required to save PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // Save dashboard view as PDF to Downloads folder
    private fun saveDashboardAsPdf() {
        val rootView = window.decorView.rootView
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "dashboard.pdf")

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
                Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    // Setup navigation menu items click listeners
    private fun setupNavigationListeners() {
        // ☰ Show side menu with options
        findViewById<ImageView>(R.id.nav_menu).setOnClickListener {
            showSideMenu()
        }
        for (item in menuItems) {
            val viewId = resources.getIdentifier(
                "dashboard_drawer_${item.title.lowercase().replace(" ", "")}", "id", packageName
            )
            val menuView = findViewById<View>(viewId)
            menuView?.setOnClickListener {
                val intent = Intent(this, item.targetActivity)
                startActivity(intent)
            }
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

    // 🗂 Data class for menu items
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}