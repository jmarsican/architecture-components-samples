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
import androidx.lifecycle.MutableLiveData
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
import com.android.example.github.util.CountingAppExecutorsRule
import com.android.example.github.util.RecyclerViewMatcher
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.android.example.github.util.TestUtil
import com.android.example.github.vo.Repo
import com.android.example.github.vo.Resource
import com.android.example.github.vo.User
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    @Rule
    @JvmField
    val executorRule = TaskExecutorWithIdlingResourceRule()
    @Rule
    @JvmField
    val countingAppExecutors = CountingAppExecutorsRule()

    private lateinit var viewModel: SearchViewModel
    private val results = MutableLiveData<Resource<List<Repo>>>()

    private val resource : IdlingResource = OkHttp3IdlingResource.create("okhttp", OkHttpProvider.instance)


    @Before
    fun init() {
//        viewModel = mock(SearchViewModel::class.java)
//        doReturn(loadMoreStatus).`when`(viewModel).loadMoreStatus
//        `when`(viewModel.results).thenReturn(results)
//
//        mockBindingAdapter = mock(FragmentBindingAdapters::class.java)
//
//        val scenario = launchFragmentInContainer(
//                themeResId = R.style.AppTheme) {
//            SearchFragment().apply {
//                appExecutors = countingAppExecutors.appExecutors
//                viewModelFactory = ViewModelUtil.createFor(viewModel)
//                dataBindingComponent = object : DataBindingComponent {
//                    override fun getFragmentBindingAdapters(): FragmentBindingAdapters {
//                        return mockBindingAdapter
//                    }
//                }
//            }
//        }
//        dataBindingIdlingResourceRule.monitorFragment(scenario)
//        scenario.onFragment { fragment ->
//            Navigation.setViewNavController(fragment.requireView(), navController)
//            fragment.disableProgressBarAnimations()
//        }
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

        val repoPosition = 1
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

        onView(RecyclerViewMatcher(R.id.repo_list).atPosition(repoPosition))
                .perform(click())

        //THEN
        onView(withId(R.id.name))
                .check(matches(withText(repo.fullName)))
        onView(withId(R.id.progress_bar))
                .check(matches(not(isDisplayed())))
    }

    @Test
    fun clickInContributor_ShowsAllHisRepos() {
        //GIVEN
        MockServer.enqueueJsonResponse("search")
        MockServer.enqueueJsonResponse("contributors")
        MockServer.enqueueJsonResponse("repos-yigit")
        MockServer.enqueueJsonResponse("user-yigit")

        val user = getUserResponseDTO()!!

        //WHEN
        onView(withId(R.id.input))
                .perform(
                        typeText("foo"),
                        pressKey(KeyEvent.KEYCODE_ENTER)
                )

        onView(withId(R.id.repo_list))
                .check(matches(isDisplayed()))

        onView(RecyclerViewMatcher(R.id.repo_list).atPosition(0))
                .perform(click())

        onView(RecyclerViewMatcher(R.id.contributor_list).atPosition(0))
                .perform(click())

        //THEN
        onView(withContentDescription("user name"))
                .check(matches(withText(user.name)))
        onView(withId(R.id.progress_bar))
                .check(matches(not(isDisplayed())))
    }

    @Test
    fun clickInSearchResults_ShowsErrorScreen() {
        ActivityScenario.launch(MainActivity::class.java)
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

    @Ignore
    @Test
    fun loadResults() {
        val repo = TestUtil.createRepo("foo", "bar", "desc")
        results.postValue(Resource.success(arrayListOf(repo)))
        onView(listMatcher().atPosition(0)).check(matches(hasDescendant(withText("foo/bar"))))
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Ignore
    @Test
    fun loadMore() {
        val repos = TestUtil.createRepos(50, "foo", "barr", "desc")
        results.postValue(Resource.success(repos))
        val action = RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(49)
        onView(withId(R.id.repo_list)).perform(action)
        onView(listMatcher().atPosition(49)).check(matches(isDisplayed()))
        verify(viewModel).loadNextPage()
    }

    private fun getRepoSearchResponseDTO() = MockServer.getObjectFromJsonFile(
                "search",
                RepoSearchResponse::class.java)

    private fun getUserResponseDTO() = MockServer.getObjectFromJsonFile(
                "user-yigit",
                User::class.java)

    private fun listMatcher(): RecyclerViewMatcher {
        return RecyclerViewMatcher(R.id.repo_list)
    }

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