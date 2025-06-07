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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username required"
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
            }
            else -> authenticateUser(username, password)
        }
    }

    private fun authenticateUser(username: String, password: String) {
        binding.btnLogin.isEnabled = false
        val firebaseAuth = FirebaseAuth.getInstance()

        userViewModel.getUserByUsername(
            username = username,
            onResult = { user ->
                if (user == null) {
                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                    // No return needed here! Just exit the lambda.
                } else {
                    // Firebase sign-in with user's email
                    firebaseAuth.signInWithEmailAndPassword(user.email, password)
                        .addOnSuccessListener {
                            Log.i(TAG, "Firebase login successful for user: ${user.email}")
                            runOnUiThread {
                                saveUserSession(user.id, user.fullName)
                                Toast.makeText(this, "Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Firebase login failed: ${e.message}")
                            runOnUiThread {
                                binding.btnLogin.isEnabled = true
                                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            onError = { error ->
                Log.e(TAG, "Error fetching user by username: $error")
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Login error: $error", Toast.LENGTH_SHORT).show()
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