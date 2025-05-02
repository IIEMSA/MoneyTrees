package com.example.moneytrees1.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moneytrees1.R
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    // Test: Empty username should show error
    @Test
    fun emptyUsername_showsError() {
        // Launch the activity
        ActivityScenario.launch(LoginActivity::class.java)

        // Type empty text and click login
        onView(withId(R.id.etUsername)).perform(typeText(""), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        // Verify error message
        onView(withId(R.id.etUsername))
            .check(matches(hasErrorText("Username required")))
    }

    // Test: Clicking register navigates to RegisterActivity
    @Test
    fun clickRegister_navigatesToRegisterScreen() {
        // Launch the activity
        ActivityScenario.launch(LoginActivity::class.java)

        // Click the register text view
        onView(withId(R.id.tvRegister)).perform(click())

        // Verify the next activity is launched
        onView(withId(R.id.etFullName)).check(matches(isDisplayed()))
    }
}