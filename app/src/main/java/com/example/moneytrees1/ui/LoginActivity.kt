package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.databinding.ActivityLoginBinding
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Activity created")

        // Initialize ViewModel with the app repository
        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            Log.d(TAG, "Register text clicked")
            navigateToRegister()
        }

        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot password clicked")
            showForgotPassword()
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        Log.d(TAG, "Attempting login for user: $username")

        when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username required"
                Log.w(TAG, "Empty username")
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                Log.w(TAG, "Empty password")
            }
            else -> authenticateUser(username, password)
        }
    }

    private fun authenticateUser(username: String, password: String) {
        Log.d(TAG, "Authenticating user: $username")
        binding.btnLogin.isEnabled = false

        userViewModel.loginUser(
            username = username,
            password = password,
            onSuccess = { user ->
                Log.i(TAG, "Login successful for user: ${user.username}")
                runOnUiThread {
                    // Save user session
                    saveUserSession(user.id)  // <-- Add this
                    showToast("Welcome ${user.fullName}!")
                    navigateToMain(user.id, user.fullName)
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Login error: $error")
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    showToast(error)
                }
            }
        )
    }

    private fun saveUserSession(userId: Int) {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", userId)
            apply()
        }
        Log.d(TAG, "User session saved for ID: $userId")
    }

    private fun navigateToMain(userId: Int, username: String) {
        Log.d(TAG, "Navigating to MainActivity with userId: $userId")
        MainActivity.start(this, userId, username) // Use the new clean method
        finish()
    }

    private fun navigateToRegister() {
        Log.d(TAG, "Navigating to RegisterActivity")
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun showForgotPassword() {
        Log.d(TAG, "Showing forgot password message")
        showToast("Password reset feature coming soon")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
