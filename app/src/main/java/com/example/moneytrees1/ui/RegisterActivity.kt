package com.example.moneytrees1.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.data.User
import com.example.moneytrees1.databinding.ActivityRegisterBinding
import com.example.moneytrees1.utils.PasswordUtils
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "RegisterActivity created")

        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            attemptRegistration()
        }

        binding.tvLogin.setOnClickListener {
            Log.d(TAG, "Login text clicked")
            navigateToLogin()
        }
    }

    private fun attemptRegistration() {
        val fullName = binding.etFullName.text.toString().trim()
        val surname = binding.etSurname.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val repeatPassword = binding.etRepeatPassword.text.toString()

        Log.d(TAG, "Attempting registration for $username")

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
                val hashedPassword = PasswordUtils.hashPassword(password)
                val user = User(
                    fullName = fullName,
                    surname = surname,
                    username = username,
                    email = email,
                    password = hashedPassword
                )
                registerUser(user)
            }
        }
    }

    private fun registerUser(user: User) {
        binding.btnRegister.isEnabled = false

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(user.email, binding.etPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase Auth: User created")

                    // Continue with local RoomDB storage
                    userViewModel.registerUser(
                        user = user,
                        onSuccess = { insertedUserId ->
                            Log.d(TAG, "User saved locally with ID $insertedUserId")

                            // Optional: clear user-specific data
                            val app = application as MyApplication
                            CoroutineScope(Dispatchers.IO).launch {
                                app.expenseRepository.clearUserExpenses(insertedUserId.toInt())
                                app.budgetRepository.clearUserBudgets(insertedUserId.toInt())
                                runOnUiThread {
                                    showToast("Registration successful! Please log in. 🎉")
                                    navigateToLogin()
                                }
                            }
                        },
                        onFailure = { error ->
                            showError("Local DB Error: $error")
                            binding.btnRegister.isEnabled = true
                            Log.e(TAG, "RoomDB Error: $error")
                        }
                    )
                } else {
                    binding.btnRegister.isEnabled = true
                    val errorMessage = task.exception?.message ?: "Unknown Firebase error"
                    showError("Firebase Error: $errorMessage")
                    Log.e(TAG, "Firebase registration failed", task.exception)
                }
            }
    }


    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        binding.tvRegisterSubtitle.apply {
            text = message
            setTextColor(getColor(android.R.color.holo_red_dark))
        }
        Log.w(TAG, "Registration error: $message")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}