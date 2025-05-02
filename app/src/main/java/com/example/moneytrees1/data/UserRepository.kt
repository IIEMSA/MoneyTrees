package com.example.moneytrees1.data

import com.example.moneytrees1.utils.PasswordUtils

/**
 * Repository that mediates between ViewModel and DAO.
 * Handles business logic and password hashing.
 */
class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Boolean {
        return if (!userDao.usernameExists(user.username) && !userDao.emailExists(user.email)) {
            userDao.insertUser(user)
            true
        } else {
            false
        }
    }

    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user) // return the ID
    }


    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}
