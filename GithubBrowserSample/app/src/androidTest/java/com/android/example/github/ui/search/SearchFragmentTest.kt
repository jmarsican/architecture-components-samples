/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.github.ui.search

import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.example.github.MainActivity
import com.android.example.github.R
import com.android.example.github.api.MockServer
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.di.OkHttpProvider
import com.android.example.github.util.RecyclerViewMatcher
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.android.example.github.vo.User
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()

    private val resource : IdlingResource =
            OkHttp3IdlingResource.create("okhttp", OkHttpProvider.instance)

    @Before
    fun init() {
        MockServer.init()
        IdlingRegistry.getInstance().register(resource)
        ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        MockServer.reset()
        IdlingRegistry.getInstance().unregister(resource)
    }

    @Test
    fun firstScreenShowsSearchOption() {
        onView(withId(R.id.textInputLayout3))
                .check(matches(isDisplayed()))
                .check(matches(hasTextInputLayoutHintText("search repositories")))
    }

    @Test
    fun clickInSearchResults_ShowsCorrectRepositoryScreen() {
        //GIVEN
        MockServer.enqueueJsonResponse("search")

        val repoPosition = 14
        val repositories = getRepoSearchResponseDTO()
        val repo = repositories!!.items[repoPosition]

        //WHEN
        onView(withId(R.id.input))
                .perform(
                        typeText("foo"),
                        pressKey(KeyEvent.KEYCODE_ENTER)
                )

        onView(withId(R.id.repo_list))
                .check(matches(isDisplayed()))

        val action = RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(repoPosition)
        onView(withId(R.id.repo_list))
                .perform(action)
        onView(RecyclerViewMatcher(R.id.repo_list).atPosition(repoPosition))
                .perform(click())

        //THEN
        onView(withId(R.id.name))
                .check(matches(withText(repo.fullName)))
        onView(withId(R.id.progress_bar))
                .check(matches(not(isDisplayed())))
    }

    @Test
    fun clickInSearchResults_ShowsErrorScreen() {
        MockServer.enqueueErrorResponse()

        onView(withId(R.id.input))
                .perform(
                        typeText("foo"),
                        pressKey(KeyEvent.KEYCODE_ENTER)
                )

        onView(withId(R.id.retry))
                .check(matches(isDisplayed()))
        onView(withId(R.id.error_msg))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))))
    }

   @Test
    fun clickInContributor_ShowsAllHisRepos() {
       //GIVEN
       val user = getUserResponseDTO()!!

       val repoPosition = 1
       val repositories = getRepoSearchResponseDTO()
       val repo = repositories!!.items[repoPosition]

       val responses = MockServer.ConditionalResponseComposite(
               "/users/${repo.owner.login}/repos","repos-yigit",
               MockServer.ConditionalResponseComposite(
                       "/users/${repo.owner.login}", "user-yigit",
                       MockServer.ConditionalResponseComposite(
                               "/repos/${repo.owner.login}/${repo.name}/contributors", "contributors",
                               MockServer.ConditionalResponseComposite("/search/repositories", "search")
                       )
               )
       )
       MockServer.enqueueConditionalResponses(responses)

        //WHEN
        onView(withId(R.id.input))
                .perform(
                        typeText("foo"),
                        pressKey(KeyEvent.KEYCODE_ENTER)
                )

        onView(withId(R.id.repo_list))
                .check(matches(isDisplayed()))

        onView(RecyclerViewMatcher(R.id.repo_list).atPosition(repoPosition))
                .perform(click())

        onView(withId(R.id.contributor_list))
                .check(matches(hasDescendant(withText(repo.owner.login))))
        onView(RecyclerViewMatcher(R.id.contributor_list).atPosition(0))
                .perform(click())

        onView(withContentDescription("user name"))
                .check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar))
                .check(matches(not(isDisplayed())))
    }

    private fun getRepoSearchResponseDTO() = MockServer.getObjectFromJsonFile(
                "search",
                RepoSearchResponse::class.java)

    private fun getUserResponseDTO() = MockServer.getObjectFromJsonFile(
                "user-yigit",
                User::class.java)

    private fun hasTextInputLayoutHintText(expected: String): Matcher<View> = object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description?) {
            description?.appendText("TextView or TextInputLayout with hint '$expected'")
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val error = item.hint ?: return false
            val hint = error.toString()
            return expected == hint
        }
    }
}