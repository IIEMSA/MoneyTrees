package com.example.moneytrees1.ui

// 🔷 Android Imports
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

// 🏗️ App-Specific Imports
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.data.User
import com.example.moneytrees1.databinding.ActivityRegisterBinding
import com.example.moneytrees1.utils.PasswordUtils
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory


class RegisterActivity : AppCompatActivity() {

    // View binding object for accessing UI elements 🖼️
    private lateinit var binding: ActivityRegisterBinding

    // ViewModel for user-related logic 🧠
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val TAG = "RegisterActivity" // Logging tag 🏷️
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding 🧩
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity created") // Log for debugging 🐞

        // Initialize ViewModel using a factory 🏭
        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        setupClickListeners() // Set up button click actions 🎯
    }

    private fun setupClickListeners() {
        // When "Register" button is clicked 📝
        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            attemptRegistration()
        }

        // When "Login" text is clicked, navigate to LoginActivity 🔐
        binding.tvLogin.setOnClickListener {
            Log.d(TAG, "Login text clicked")
            navigateToLogin()
        }
    }

    private fun attemptRegistration() {
        // Get user input from fields 📥
        val fullName = binding.etFullName.text.toString().trim()
        val surname = binding.etSurname.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val repeatPassword = binding.etRepeatPassword.text.toString()

        Log.d(TAG, "Attempting registration for $username")

        // Validate user input ✅
        when {
            fullName.isEmpty() -> showError("Full name is required ⚠️")
            surname.isEmpty() -> showError("Surname is required ⚠️")
            username.isEmpty() -> showError("Username is required ⚠️")
            email.isEmpty() -> showError("Email is required ⚠️")
            password.isEmpty() -> showError("Password is required ⚠️")
            password != repeatPassword -> showError("Passwords don't match 🔁")
            password.length < 6 -> showError("Password must be at least 6 characters 🔐")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                showError("Invalid email format 📧")
            else -> {
                // Hash the password before saving 🔒
                val hashedPassword = PasswordUtils.hashPassword(password)

                // Create a User object 👤
                val user = User(
                    fullName = fullName,
                    surname = surname,
                    username = username,
                    email = email,
                    password = hashedPassword
                )

                registerUser(user) // Proceed with registration 🚀
            }
        }
    }

    private fun registerUser(user: User) {
        binding.btnRegister.isEnabled = false // Disable button to prevent multiple taps ⛔

        // Call ViewModel to register user 📡
        userViewModel.registerUser(
            user = user,
            onSuccess = { insertedUserId ->
                Log.i(TAG, "Registration successful for ${user.username} with ID: $insertedUserId")
                runOnUiThread {
                    showToast("Registration successful! Please log in. 🎉")
                    // Navigate to login screen after success 🔁
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        putExtra("REGISTERED_USER_ID", insertedUserId)
                    }
                    startActivity(intent)
                    finish() // Close this activity 🏁
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Registration failed: $error")
                runOnUiThread {
                    binding.btnRegister.isEnabled = true // Re-enable button ❗
                    showError(error) // Show error message ❌
                }
            }
        )
    }

    private fun navigateToLogin() {
        Log.d(TAG, "Navigating to LoginActivity")
        startActivity(Intent(this, LoginActivity::class.java)) // Intent to LoginActivity 🔐
        finish()
    }

    // Display error message in subtitle field and color it red ❗
    private fun showError(message: String) {
        binding.tvRegisterSubtitle.apply {
            text = message
            setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    // Show a toast message (small popup) 🍞
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}