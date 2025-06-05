package com.example.moneytrees1.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.databinding.ActivityForgotPasswordBinding
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        binding.btnResetPassword.setOnClickListener {
            attemptResetPassword()
        }
    }

    private fun attemptResetPassword() {
        val usernameOrEmail = binding.etUsernameEmail.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        when {
            usernameOrEmail.isEmpty() -> {
                binding.etUsernameEmail.error = "Username or email required"
            }
            newPassword.isEmpty() -> {
                binding.etNewPassword.error = "New password required"
            }
            newPassword.length < 6 -> {
                binding.etNewPassword.error = "Password must be at least 6 characters"
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Confirm password required"
            }
            newPassword != confirmPassword -> {
                binding.etConfirmPassword.error = "Passwords do not match"
            }
            else -> resetPassword(usernameOrEmail, newPassword)
        }
    }

    private fun resetPassword(usernameOrEmail: String, newPassword: String) {
        userViewModel.resetPassword(
            usernameOrEmail = usernameOrEmail,
            newPassword = newPassword,
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
                    finish()
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