package com.android.example.github.ui.search.robots

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.android.example.github.R

class RepositoriesRobot: BaseRobot() {

    fun checkRepoNameIs(name: String): RepositoriesRobot {
        onView(withId(R.id.name))
            .check(matches(withText(name)))
        return this
    }

    fun clickOnContributor(text: String): ContributorRobot {
        val clickOnListItem = RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            hasDescendant(withText(text)),
            click()
        )
        onView(withId(R.id.contributor_list))
            .perform(clickOnListItem)
        return ContributorRobot()
    }

}