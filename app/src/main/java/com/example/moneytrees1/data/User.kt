package com.example.moneytrees1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a User entity in the Room database.
 * Expanded to include all required fields for registration and login.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val surname: String,
    val username: String,
    val email: String,
    val password: String // Note: Should be stored hashed in production
) {
    /**
     * Validates that all required fields are properly filled
     * @return Boolean indicating if user data is valid
     */
    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                surname.isNotBlank() &&
                username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                password.length >= 6 // Basic password length check
    }

    /**
     * Checks if email format is valid
     * @return Boolean indicating valid email format
     */
    fun hasValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}