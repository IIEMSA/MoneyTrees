package com.example.moneytrees1.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.viewmodels.BudgetViewModel
import com.example.moneytrees1.viewmodels.BudgetViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

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

    private lateinit var chart: BarChart
    private lateinit var etStart: EditText
    private lateinit var etEnd: EditText
    private lateinit var btnRefresh: Button
    private lateinit var legendContainer: LinearLayout
    private lateinit var btnDownloadPdf: Button
    private lateinit var minMaxLegend: TextView
    private lateinit var budgetViewModel: BudgetViewModel

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val categoryColorIds = listOf(
        R.color.cat1, R.color.cat2, R.color.cat3, R.color.cat4, R.color.cat5,
        R.color.cat6, R.color.cat7, R.color.cat8, R.color.cat9, R.color.cat10,
        R.color.cat11, R.color.cat12, R.color.cat13, R.color.cat14, R.color.cat15,
        R.color.cat16, R.color.cat17, R.color.cat18, R.color.cat19, R.color.cat20
    )
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // SESSION: Only from SharedPreferences
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize ViewModel
        val app = application as MyApplication
        budgetViewModel = ViewModelProvider(
            this,
            BudgetViewModelFactory(app.budgetRepository, app.expenseRepository, userId)
        )[BudgetViewModel::class.java]

        // Init UI
        chart = findViewById(R.id.barChart)
        etStart = findViewById(R.id.et_start_date)
        etEnd = findViewById(R.id.et_end_date)
        btnRefresh = findViewById(R.id.btnRefresh)
        legendContainer = findViewById(R.id.legendContainer)
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf)
        minMaxLegend = findViewById(R.id.minmax_legend)

        // Set default date range (current month)
        val now = Calendar.getInstance()
        now.set(Calendar.DAY_OF_MONTH, 1)
        etStart.setText(dateFormat.format(now.time))
        now.time = Date()
        etEnd.setText(dateFormat.format(now.time))

        etStart.setOnClickListener { pickDateDialog(etStart) }
        etEnd.setOnClickListener { pickDateDialog(etEnd) }
        btnRefresh.setOnClickListener { loadAndPopulateChart() }
        btnDownloadPdf.setOnClickListener { checkAndSavePdf() }

        setupNavigationListeners()

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

        budgetViewModel.currentBudget.observe(this, Observer { _ ->
            loadAndPopulateChart()
        })
    }

    private fun pickDateDialog(editText: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this, { _, y, m, d ->
                val str = String.format("%04d-%02d-%02d", y, m + 1, d)
                editText.setText(str)
                loadAndPopulateChart()
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadAndPopulateChart() {
        lifecycleScope.launch {
            val start = etStart.text.toString()
            val end = etEnd.text.toString()
            val app = application as MyApplication

            val categories = app.categoryRepository.getAllCategories(userId).map { it.name }.distinct()
            val expenses = app.expenseRepository.getAllExpenses(userId)
            val expensesByCat = categories.associateWith { cat ->
                expenses.filter { it.category == cat && it.date >= start && it.date <= end }
                    .sumOf { it.amount }
            }

            val budget = budgetViewModel.currentBudget.value
            val minGoal = budget?.minimumGoal ?: 0.0
            val maxGoal = budget?.maximumGoal ?: 0.0

            updateGraph(categories, expensesByCat, minGoal, maxGoal)
            buildLegend(categories)
        }
    }

    private fun updateGraph(
        categories: List<String>,
        expensesByCat: Map<String, Double>,
        minGoal: Double,
        maxGoal: Double
    ) {
        val entries = categories.mapIndexed { i, cat ->
            BarEntry(i.toFloat(), expensesByCat[cat]?.toFloat() ?: 0f)
        }

        val thousandsFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000 -> "%.1fM".format(value / 1_000_000)
                    value >= 1_000 -> "%.1fK".format(value / 1_000)
                    else -> "%.0f".format(value)
                }
            }
        }

        val barDataSet = BarDataSet(entries, "Spending per Category").apply {
            setColors(getCategoryColors(entries.size))
            valueTextSize = 11f
            valueFormatter = thousandsFormatter
        }

        val barData = BarData(barDataSet)
        barData.barWidth = 0.7f

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(categories)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 11f
        xAxis.labelRotationAngle = -45f

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

        chart.data = barData
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(900)
        chart.invalidate()
    }

    private fun getCategoryColors(size: Int): List<Int> =
        (0 until size).map { i ->
            ContextCompat.getColor(this, categoryColorIds[i % categoryColorIds.size])
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveDashboardAsPdf()
        } else {
            Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDashboardAsPdf() {
        val rootView = findViewById<LinearLayout>(R.id.dashboard_root)
        val bitmap = getBitmapFromView(rootView)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        val fileName = "Dashboard_${System.currentTimeMillis()}.pdf"
        val file: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
        }

        try {
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(this, "Saved PDF to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        view.draw(c)
        return b
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