package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.R
import com.example.moneytrees1.data.User
import com.example.moneytrees1.databinding.ActivityProfileBinding
import com.example.moneytrees1.ui.MainActivity.MenuItem
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory

class ProfileActivity : AppCompatActivity() {

    // Side menu navigation options
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

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userViewModel: UserViewModel
    private var isEditing = false
    private var currentUser: User? = null

    companion object {
        private const val TAG = "ProfileActivity"
        const val EXTRA_USER_ID = "USER_ID"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Activity created")

        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        var userId = intent.getIntExtra(EXTRA_USER_ID, -1)

        // Fallback to shared preferences if ID not found in intent
        if (userId == -1) {
            userId = getUserIdFromSession() // You need to implement this
        }

        if (userId == -1) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserData(userId)
        setupClickListeners()
    }

    // Add this function to get user ID from shared preferences
    private fun getUserIdFromSession(): Int {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1) // Returns -1 if not found
    }

    private fun loadUserData(userId: Int) {
        userViewModel.getUserById(
            userId,
            onSuccess = { user ->
                currentUser = user
                runOnUiThread {
                    binding.etName.setText(user.fullName)
                    binding.etSurname.setText(user.surname)
                    binding.etUsername.setText(user.username)
                    binding.etEmail.setText(user.email)
                    binding.etPassword.setText("********") // Mask password initially

                    // Disable editing fields by default
                    disableEditing()
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load user: $error")
                runOnUiThread {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )
    }


    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            if (isEditing) {
                saveProfileChanges()
            } else {
                enterEditMode()
            }
        }

        binding.switchShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.setSelection(binding.etPassword.length())
        }

        binding.tvLogout.setOnClickListener {
            logout()
        }
    }

    private fun enterEditMode() {
        isEditing = true
        binding.btnEditProfile.text = getString(R.string.save_profile)

        // Enable editing fields
        binding.etName.isEnabled = true
        binding.etSurname.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etPassword.isEnabled = true

        // Show actual password when editing
        currentUser?.let {
            binding.etPassword.setText(it.password)
        }
    }

    private fun saveProfileChanges() {
        val fullName = binding.etName.text.toString().trim()
        val surname = binding.etSurname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (fullName.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            userViewModel.updateProfile(
                currentUsername = user.username,
                fullName = fullName,
                surname = surname,
                email = email,
                newPassword = if (password != user.password) password else "",
                onSuccess = {
                    runOnUiThread {
                        isEditing = false
                        binding.btnEditProfile.text = getString(R.string.edit_profile)
                        disableEditing()
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { error ->
                    runOnUiThread {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun disableEditing() {
        binding.etName.isEnabled = false
        binding.etSurname.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPassword.isEnabled = false
        binding.etPassword.setText("********") // Mask password again
    }

    private fun logout() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish() // finish after starting new activity
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
