package com.android.example.github.ui.search.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText

class ContributorRobot: BaseRobot() {

    fun checkUserNameIs(name: String?): ContributorRobot {
        onView(withContentDescription("user name"))
            .check(matches(withText(name)))
        return this
    }

}