package com.example.moneytrees1.ui

// 🏗️ Android Framework
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*

// 🏛️ AndroidX Libraries
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

// 🏠 App Components
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.data.ExpenseEntity
import com.example.moneytrees1.viewmodels.ExpenseViewModel
import com.example.moneytrees1.viewmodels.ExpenseViewModelFactory

// 🔥 Firebase Services
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// 🌀 Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// ⏰ Java Utilities
import java.util.*


class ExpenseActivity : AppCompatActivity() {

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

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var selectImageButton: Button
    private lateinit var imageNameTextView: TextView
    private var imageUri: Uri? = null
    private var uploadedImageUrl: String? = null // URL from Firebase Storage
    private var userId: Int = -1

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        // SESSION: Get userId ONLY from SharedPreferences
        userId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val app = application as MyApplication
        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(app.expenseRepository, userId)
        )[ExpenseViewModel::class.java]

        setupNavigationListeners()

        val etName = findViewById<EditText>(R.id.et_expense_name)
        val etAmount = findViewById<EditText>(R.id.et_expense_amount)
        val spinnerCategory = findViewById<Spinner>(R.id.spinner_category)
        val etDate = findViewById<EditText>(R.id.et_expense_date)
        val etStartTime = findViewById<EditText>(R.id.et_start_time)
        val etEndTime = findViewById<EditText>(R.id.et_end_time)
        val btnSave = findViewById<Button>(R.id.btn_save_expense)
        selectImageButton = findViewById(R.id.select_image_button)
        imageNameTextView = findViewById(R.id.image_name_textview)

        // Load per-user categories and populate spinner
        CoroutineScope(Dispatchers.IO).launch {
            val categories = app.categoryRepository.getAllCategories(userId)
            val categoryNames = categories.map { it.name }
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@ExpenseActivity,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }
        }

        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Date Picker
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val dateString = "%04d-%02d-%02d".format(year, month + 1, day)
                    etDate.setText(dateString)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Time Pickers
        etStartTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                    etStartTime.setText(timeString)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        etEndTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    etEndTime.setText(timeString)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val category = spinnerCategory.selectedItem?.toString()
            val amountStr = etAmount.text.toString()
            val date = etDate.text.toString()
            val startTime = etStartTime.text.toString()
            val endTime = etEndTime.text.toString()

            if (!category.isNullOrBlank() &&
                amountStr.isNotBlank() &&
                date.isNotBlank() &&
                startTime.isNotBlank() &&
                endTime.isNotBlank()
            ) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // Upload image if selected and get URL
                        if (imageUri != null) {
                            uploadedImageUrl = uploadImageToFirebase(imageUri!!)
                        }

                        // Create expense object with uploadedImageUrl
                        val expense = ExpenseEntity(
                            userId = userId,
                            amount = amount,
                            name = name,
                            category = category,
                            date = date,
                            description = name,
                            startTime = startTime,
                            endTime = endTime,
                            imagePath = uploadedImageUrl // store Firebase URL here instead of local path
                        )

                        // Save locally (Room DB)
                        expenseViewModel.insertExpense(expense)

                        // Save to Firestore
                        saveExpenseToFirestore(expense)

                        Toast.makeText(this@ExpenseActivity, "Expense added", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@ExpenseActivity, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun uploadImageToFirebase(uri: Uri): String {
        val fileName = "expense_images/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun saveExpenseToFirestore(expense: ExpenseEntity) {
        // Use Firestore collection "expenses" with document id auto-generated
        val expenseMap = hashMapOf(
            "userId" to expense.userId,
            "amount" to expense.amount,
            "name" to expense.name,
            "category" to expense.category,
            "date" to expense.date,
            "description" to expense.description,
            "startTime" to expense.startTime,
            "endTime" to expense.endTime,
            "imageUrl" to expense.imagePath // Firebase URL
        )

        firestore.collection("expenses")
            .add(expenseMap)
            .addOnSuccessListener {
                // optional: Log or update UI
            }
            .addOnFailureListener {
                // optional: Log error
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    imageUri = uri
                    imageNameTextView.text = uri.lastPathSegment ?: "Image selected"
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
                val intent = Intent(this, menuItems[which].targetActivity)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class MenuItem(val title: String, val targetActivity: Class<*>)
}