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

        Log.d(TAG, "LoginActivity created")

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
            Log.d(TAG, "Register link clicked")
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot password clicked")
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username required"
                Log.w(TAG, "Username is empty")
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                Log.w(TAG, "Password is empty")
            }
            else -> authenticateUser(username, password)
        }
    }

    private fun authenticateUser(username: String, password: String) {
        binding.btnLogin.isEnabled = false
        Log.d(TAG, "Authenticating user: $username")

        userViewModel.loginUser(
            username = username,
            password = password,
            onSuccess = { user ->
                Log.i(TAG, "Login successful for user: ${user.username} (userId=${user.id})")
                runOnUiThread {
                    saveUserSession(user.id, user.fullName)
                    Toast.makeText(this, "Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Login failed: $error")
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun saveUserSession(userId: Int, fullName: String) {
        getSharedPreferences("user_session", MODE_PRIVATE).edit()
            .putInt("USER_ID", userId)
            .putString("USERNAME", fullName)
            .apply()
        Log.d(TAG, "User session saved for userId: $userId, fullName: $fullName")
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}