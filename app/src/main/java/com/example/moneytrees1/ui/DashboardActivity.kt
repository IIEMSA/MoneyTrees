package com.example.moneytrees1.ui

// Android imports
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.moneytrees1.R
import com.example.moneytrees1.ui.MainActivity.MenuItem

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

// Java imports
import java.io.File
import java.io.FileOutputStream


class DashboardActivity : AppCompatActivity() {

    // Side menu navigation options
    private val menuItems = listOf(
        com.example.moneytrees1.ui.MainActivity.MenuItem("Home", MainActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Dashboard",
            DashboardActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem("Profile", ProfileActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Add Expense",
            ExpenseActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Budget Planner",
            BudgetPlannerActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Expense History",
            ExpenseHistoryActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Achievements",
            AchievementsActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Leaderboard",
            LeaderboardActivity::class.java
        ),
        com.example.moneytrees1.ui.MainActivity.MenuItem("Game", GameActivity::class.java),
        com.example.moneytrees1.ui.MainActivity.MenuItem(
            "Add Category",
            CategoryActivity::class.java
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 🔹 Setup pie and bar charts
        setupPieChart()
        setupBarChart()

        // 🔹 Setup Download Stats button
        val btnDownloadStats: Button = findViewById(R.id.btnDownloadStats)
        btnDownloadStats.setOnClickListener {
            downloadStatsAsPdf()
        }
    }

    /**
     * 🔧 Set up Pie Chart with sample spending data
     */
    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // Sample percentage data (can adjust values as needed)
        val entries = listOf(
            PieEntry(20f, "Housing"),
            PieEntry(10f, "Utilities"),
            PieEntry(15f, "Groceries"),
            PieEntry(10f, "Transportation"),
            PieEntry(5f, "Healthcare"),
            PieEntry(10f, "Savings"),
            PieEntry(10f, "Leisure"),
            PieEntry(5f, "Subscriptions"),
            PieEntry(5f, "Clothing"),
            PieEntry(10f, "Miscellaneous")
        )

        val dataSet = PieDataSet(entries, "Spending Breakdown")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Monthly Expenses"
        pieChart.setCenterTextSize(14f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }


    /**
     * 🔧 Set up Bar Chart with sample category data
     */
    private fun setupBarChart() {
        val barChart = findViewById<BarChart>(R.id.barChart)

        // Sample values per category (can be real or mock data)
        val entries = listOf(
            BarEntry(0f, 800f),  // Housing
            BarEntry(1f, 150f),  // Utilities
            BarEntry(2f, 300f),  // Groceries
            BarEntry(3f, 100f),  // Transportation
            BarEntry(4f, 75f),   // Healthcare
            BarEntry(5f, 200f),  // Savings
            BarEntry(6f, 120f),  // Leisure
            BarEntry(7f, 50f),   // Subscriptions
            BarEntry(8f, 90f),   // Clothing
            BarEntry(9f, 100f)   // Miscellaneous
        )

        val dataSet = BarDataSet(entries, "Category Totals")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(dataSet)
        barData.setValueTextSize(12f)
        barData.setValueTextColor(Color.BLACK)

        val labels = listOf(
            "Housing", "Utilities", "Groceries", "Transportation",
            "Healthcare", "Savings", "Leisure", "Subscriptions", "Clothing", "Misc."
        )

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawLabels(true)
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false

        barChart.description = Description().apply { text = "" }
        barChart.data = barData
        barChart.animateY(1000)
        barChart.invalidate()
    }


    /**
     * 📄 Create and save a basic PDF with sample financial text data
     */
    private fun downloadStatsAsPdf() {
        val filePath = File(filesDir, "stats_report.pdf")

        try {
            val pdfDocument = PdfDocument()

            // Create paint object to define text style
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                textAlign = Paint.Align.LEFT
            }

            // A4 size PDF page
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // Draw text content
            val canvas = page.canvas
            canvas.drawText("Financial Stats Report", 100f, 100f, paint)
            canvas.drawText("Category: Food - $100", 100f, 150f, paint)
            canvas.drawText("Category: Entertainment - $50", 100f, 200f, paint)
            canvas.drawText("Category: Transport - $40", 100f, 250f, paint)

            pdfDocument.finishPage(page)

            // Save to internal storage
            pdfDocument.writeTo(FileOutputStream(filePath))
            pdfDocument.close()

            Toast.makeText(this, "Stats PDF saved to: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()

            // Optionally open the PDF
            // openPdf(filePath)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 📂 Opens the generated PDF using external viewer
     */
    private fun openPdf(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    private fun setupNavigationListeners() {
        // Side menu
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

    // Simple data class for menu items
    data class MenuItem(val title: String, val targetActivity: Class<*>)
}