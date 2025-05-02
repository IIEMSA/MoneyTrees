package com.example.moneytrees1.utils

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Password security utilities using BCrypt hashing.
 */
object PasswordUtils {
    private const val BCRYPT_COST = 12 // Security vs performance tradeoff

    /**
     * Hashes a plaintext password
     * @param password Plaintext password
     * @return Hashed password string
     */
    // Properly uses BCrypt for secure hashing
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }

    /**
     * Verifies password against hash
     * @param password Plaintext password
     * @param hash Stored password hash
     * @return Boolean indicating match
     */
    // Correct verification using BCrypt
    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}