package com.example.moneytrees1.ui

// 📦 Android core imports
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*

// 🎯 AndroidX libraries
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

// 🧠 App-specific imports
import com.example.moneytrees1.R
import com.example.moneytrees1.data.AppDatabase
import com.example.moneytrees1.data.ExpenseEntity
import com.example.moneytrees1.ui.MainActivity.MenuItem

// ⚙️ Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 📆 Utility
import java.util.*

class ExpenseActivity : AppCompatActivity() {

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

    private val db by lazy { AppDatabase.getDatabase(this) }

    private val IMAGE_REQUEST_CODE = 1001
    private lateinit var selectImageButton: Button
    private lateinit var imageNameTextView: TextView
    private var imageUri: Uri? = null
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        val etName = findViewById<EditText>(R.id.et_expense_name)
        val etAmount = findViewById<EditText>(R.id.et_expense_amount)
        val spinnerCategory = findViewById<Spinner>(R.id.spinner_category)
        val etDate = findViewById<EditText>(R.id.et_expense_date)
        val etStartTime = findViewById<EditText>(R.id.et_start_time)
        val etEndTime = findViewById<EditText>(R.id.et_end_time)
        val btnSave = findViewById<Button>(R.id.btn_save_expense)
        selectImageButton = findViewById(R.id.select_image_button)
        imageNameTextView = findViewById(R.id.image_name_textview)

        // Load categories from DB and populate spinner
        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.categoryDao().getAllCategories()
            val categoryNames = categories.map { it.name }

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@ExpenseActivity, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
        }

        // Set onClick listener to allow the user to select an image
        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Date Picker for Date
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this,
                { _, year, month, day ->
                    val dateString = "$year-${month + 1}-$day"
                    etDate.setText(dateString)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Picker for Start Time
        etStartTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    val timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    etStartTime.setText(timeString)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Time Picker for End Time
        etEndTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    etEndTime.setText(timeString)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Save Button Click
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val category = spinnerCategory.selectedItem?.toString()
            val amountStr = etAmount.text.toString()
            val date = etDate.text.toString()
            val startTime = etStartTime.text.toString()
            val endTime = etEndTime.text.toString()

            if (!category.isNullOrBlank() && amountStr.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val expense = ExpenseEntity(
                    name = name,
                    category = category,
                    amount = amount,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    imagePath = imagePath // Store the image path (or handle blob storage if needed)
                )

                CoroutineScope(Dispatchers.IO).launch {
                    db.expenseDao().insertExpense(expense)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ExpenseActivity, "Expense added", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to handle image selection from gallery
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    // Launches the activity for selecting an image from gallery
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { uri ->
                imageUri = uri
                imagePath = getRealPathFromURI(uri)  // Get the actual path of the image
                imageNameTextView.text = imagePath?.substringAfterLast("/") // Display file name
            }
        }
    }

    // Function to get the file path of the selected image
    private fun getRealPathFromURI(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: -1)
        cursor?.close()
        return filePath
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

