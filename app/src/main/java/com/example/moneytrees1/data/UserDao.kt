package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entity with all required operations
 * for registration, login, and profile management.
 */
@Dao
interface UserDao {
    /* ============ CREATE OPERATIONS ============ */
    @Insert
    suspend fun insertUser(user: User): Long


    /* ============ READ OPERATIONS ============ */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    /* ============ VALIDATION OPERATIONS ============ */
    @Query("SELECT EXISTS(SELECT * FROM users WHERE username = :username)")
    suspend fun usernameExists(username: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    /* ============ UPDATE OPERATIONS ============ */
    @Update
    suspend fun updateUser(user: User)
}