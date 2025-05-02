package com.example.moneytrees1.viewmodels

import com.example.moneytrees1.data.User
import com.example.moneytrees1.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UserViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock()
        viewModel = UserViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registerUser should call repository insertUser`() = runTest {
        // Given
        val testUser = User(
            fullName = "Test",
            surname = "User",
            username = "testuser",
            email = "test@example.com",
            password = "hashedpassword"
        )

        whenever(userRepository.getUserByEmail(testUser.email)).thenReturn(null)

        // When
        viewModel.registerUser(
            user = testUser,
            onSuccess = { /* Do nothing */ },
            onFailure = { /* Do nothing */ }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(userRepository).insertUser(testUser)
    }

    @Test
    fun `loginUser should verify password`() = runTest {
        // Given
        val testUser = User(
            fullName = "Test",
            surname = "User",
            username = "testuser",
            email = "test@example.com",
            password = "correct_password_hash"
        )

        whenever(userRepository.getUserByEmail(testUser.email)).thenReturn(testUser)

        // When
        var result: User? = null
        viewModel.loginUser(
            username = testUser.email,
            password = "correct_password",
            onSuccess = { user -> result = user },
            onFailure = { _ -> }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assert(result != null)
        assert(result?.email == testUser.email)
    }
}