package com.android.example.github.ui.search.robots

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.android.example.github.R
import com.android.example.github.ui.search.matchers.hasTextInputLayoutHintText
import com.android.example.github.util.RecyclerViewMatcher
import org.hamcrest.Matchers.not

class SearchRepoRobot: BaseRobot() {

    fun search(input: String): SearchRepoRobot {
        onView(withId(R.id.input))
            .perform(
                typeText(input),
                pressKey(KeyEvent.KEYCODE_ENTER)
            )
        return this
    }

    fun checkTextInputWithHint(text: String): SearchRepoRobot {
        onView(withId(R.id.textInputLayout3))
            .check(matches(isDisplayed()))
            .check(matches(hasTextInputLayoutHintText(text)))
        return this
    }

    fun checkResultsDisplayed(): SearchRepoRobot {
        onView(withId(R.id.repo_list))
            .check(matches(isDisplayed()))
        return this
    }

    fun clickOnListItemAtPosition(position: Int): RepositoriesRobot {
        val scrollToListItem = RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position)
        val clickOnListItem = RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click())
        onView(withId(R.id.repo_list))
            .perform(scrollToListItem)
            .perform(clickOnListItem)
        return RepositoriesRobot()
    }

    fun clickOnListItemAtPosition_BasicAdapter(position: Int): RepositoriesRobot {
        onView(RecyclerViewMatcher(R.id.repo_list).atPosition(position))
            .perform(click())
        return RepositoriesRobot()
    }

    fun checkRetryButton(): SearchRepoRobot {
        onView(withId(R.id.retry))
            .check(matches(isDisplayed()))
        return this
    }

    fun checkErrorMessageDisplayed(): SearchRepoRobot {
        onView(withId(R.id.error_msg))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(""))))
        return this
    }

}