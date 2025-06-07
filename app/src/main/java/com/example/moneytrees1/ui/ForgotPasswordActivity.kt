package com.example.moneytrees1.ui

// Android Framework imports 🛠️
import android.os.Bundle
import android.widget.Toast

// AndroidX / AppCompat imports 📦
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

// My Application imports 🏗️
import com.example.moneytrees1.MyApplication
import com.example.moneytrees1.databinding.ActivityForgotPasswordBinding

// ViewModel imports 🧠
import com.example.moneytrees1.viewmodels.UserViewModel
import com.example.moneytrees1.viewmodels.UserViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var userViewModel: UserViewModel

    // 🏁 Activity lifecycle starts here
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* 👷‍♂️ Initialize ViewModel with factory from application */
        val app = application as MyApplication
        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(app.userRepository)
        )[UserViewModel::class.java]

        // 🎯 Set click listener on reset password button
        binding.btnResetPassword.setOnClickListener {
            attemptResetPassword()
        }
    }

    /* 🔍 Validate user input before resetting password     */
    private fun attemptResetPassword() {
        val usernameOrEmail = binding.etUsernameEmail.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        when {
            usernameOrEmail.isEmpty() -> {
                // ⚠️ Show error if username/email is missing
                binding.etUsernameEmail.error = "Username or email required"
            }
            newPassword.isEmpty() -> {
                // ⚠️ Show error if new password is missing
                binding.etNewPassword.error = "New password required"
            }
            newPassword.length < 6 -> {
                // ⚠️ Password length must be at least 6 characters
                binding.etNewPassword.error = "Password must be at least 6 characters"
            }
            confirmPassword.isEmpty() -> {
                // ⚠️ Confirm password field is required
                binding.etConfirmPassword.error = "Confirm password required"
            }
            newPassword != confirmPassword -> {
                // 🚫 Passwords must match
                binding.etConfirmPassword.error = "Passwords do not match"
            }
            else -> resetPassword(usernameOrEmail, newPassword) // ✅ All good, proceed!
        }
    }

    /* 💾 Call ViewModel to reset password and handle callbacks */
    private fun resetPassword(usernameOrEmail: String, newPassword: String) {
        userViewModel.resetPassword(
            usernameOrEmail = usernameOrEmail,
            newPassword = newPassword,
            onSuccess = {
                runOnUiThread {
                    // 🎉 Notify user of success and close activity
                    Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onFailure = { error ->
                runOnUiThread {
                    // ❌ Show error message on failure
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}