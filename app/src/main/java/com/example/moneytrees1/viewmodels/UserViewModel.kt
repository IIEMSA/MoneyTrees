package com.example.moneytrees1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrees1.data.User
import com.example.moneytrees1.data.UserRepository
import com.example.moneytrees1.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**

ViewModel for managing User-related data and business logic.

Interacts with the repository and exposes functions for login, registration, profile updates, etc.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    /**

    Logs a user in by verifying username and password.
    @param username Username entered by the user
    @param password Plaintext password entered
    @param onSuccess Callback invoked when login succeeds
    @param onFailure Callback invoked with an error message if login fails
     */
    fun loginUser(
        username: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
// Lookup user by username
                val user = repository.getUserByUsername(username)
// Verify password using BCrypt
                if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
                    onSuccess(user)
                } else {
                    onFailure("Invalid username or password")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "An error occurred")
            }
        }
    }
    /**

    Retrieves user by username (for other parts of the app).
    @param username The username to search
    @param onResult Callback with the user object (null if not found)
    @param onError Callback with error message
     */
    fun getUserByUsername(
        username: String,
        onResult: (User?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserByUsername(username)
                onResult(user)
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }
    /**

    Registers a new user if no user with the same email exists.
    Password is hashed before being stored.
    @param user User object to register
    @param onSuccess Callback invoked if registration succeeds
    @param onFailure Callback with error message if registration fails
     */
    fun registerUser(
        user: User,
        onSuccess: (Long) -> Unit, // Changed to pass insertedUserId
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = repository.getUserByEmail(user.email)
                if (existingUser == null) {
                    val insertedUserId = repository.insertUser(user)
                    onSuccess(insertedUserId) // Pass inserted ID
                } else {
                    onFailure("User already exists")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Registration failed")
            }
        }
    }
    /**

    Gets the current user by email (used in profile or session management).
    @param username Username to search for
    @param onSuccess Callback with user details
    @param onFailure Callback with error message
     */
    fun getCurrentUser(
        username: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserByEmail(username)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("User not found")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "An error occurred")
            }
        }
    }
    /**

    Updates a user's profile info.
    If a new password is provided, it will be securely hashed.
    @param currentUsername Username to identify the user
    @param fullName New full name
    @param surname New surname
    @param email New email
    @param newPassword New password (optional)
    @param onSuccess Callback invoked if update succeeds
    @param onFailure Callback with error message if update fails
     */
    fun updateProfile(
        currentUsername: String,
        fullName: String,
        surname: String,
        email: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserByUsername(currentUsername)
                if (user != null) {
// Copy user with updated values, hash password if changed
                    val updatedUser = user.copy(
                        fullName = fullName,
                        surname = surname,
                        email = email,
                        password = if (newPassword.isNotEmpty()) PasswordUtils.hashPassword(newPassword) else user.password
                    )
                    repository.updateUser(updatedUser)
                    onSuccess()
                } else {
                    onFailure("User not found")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "An error occurred")
            }
        }
    }
    fun getUserById(
        userId: Int,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserById(userId)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure("User not found")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "An error occurred")
            }
        }
    }

    fun resetPassword(
        usernameOrEmail: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var user = repository.getUserByUsername(usernameOrEmail)
                if (user == null) {
                    user = repository.getUserByEmail(usernameOrEmail)
                }
                if (user != null) {
                    val hashedPassword = PasswordUtils.hashPassword(newPassword)
                    repository.updateUser(user.copy(password = hashedPassword))
                    onSuccess()
                } else {
                    onFailure("User not found. Please check your username/email.")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Password reset failed")
            }
        }
    }
}