package com.android.example.github.ui.search.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.android.example.github.R
import org.hamcrest.CoreMatchers.not

open class BaseRobot {

    fun checkProgressBarNotDisplayed(): BaseRobot {
        onView(withId(R.id.progress_bar))
            .check(matches(not(isDisplayed())))
        return this
    }

}