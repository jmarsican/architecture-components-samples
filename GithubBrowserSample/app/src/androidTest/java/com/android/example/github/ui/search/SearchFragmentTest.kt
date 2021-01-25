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

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.example.github.MainActivity
import com.android.example.github.api.MockServer
import com.android.example.github.api.RepoSearchResponse
import com.android.example.github.di.OkHttpProvider
import com.android.example.github.ui.search.robots.SearchRepoRobot
import com.android.example.github.util.ClearDatabaseRule
import com.android.example.github.util.TaskExecutorWithIdlingResourceRule
import com.android.example.github.vo.User
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        SearchRepoRobot()
            .checkTextInputWithHint("search repositories")
    }

    @Test
    fun clickInSearchResults_ShowsCorrectRepositoryScreen() {
        //GIVEN
        val searchRepoRobot = SearchRepoRobot()

        MockServer.enqueueJsonResponse("search")

        val repoPosition = 14
        val repositories = getRepoSearchResponseDTO()
        val repo = repositories!!.items[repoPosition]

        //WHEN
        searchRepoRobot.search("foo")
            .checkResultsDisplayed()
            .clickOnListItemAtPosition(repoPosition)

        //THEN
            .checkRepoNameIs(repo.fullName)
            .checkProgressBarNotDisplayed()
    }

    @Test
    fun clickInSearchResults_ShowsErrorScreen() {
        //GIVEN
        val searchRepoRobot = SearchRepoRobot()

        ClearDatabaseRule()
                .excludeTablesMatching("android_metadata|room_master_table")
                .clearDatabases()

        MockServer.enqueueErrorResponse()

        //WHEN
        searchRepoRobot.search("foo")

            //THEN
            .checkRetryButton()
            .checkErrorMessageDisplayed()
    }

   @Test
    fun clickInContributor_ShowsAllHisRepos() {
       //GIVEN
       val searchRepoRobot = SearchRepoRobot()

       val user = getUserResponseDTO()!!
       val repoPosition = 1
       val repo = getRepoSearchResponseDTO()!!.items[repoPosition]

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
       searchRepoRobot.search("foo")
           .clickOnListItemAtPosition_BasicAdapter(repoPosition)
           .clickOnContributor(repo.owner.login)

       //THEN
           .checkUserNameIs(user.name)
           .checkProgressBarNotDisplayed()
    }

    private fun getRepoSearchResponseDTO() = MockServer.getObjectFromJsonFile(
                "search",
                RepoSearchResponse::class.java)

    private fun getUserResponseDTO() = MockServer.getObjectFromJsonFile(
                "user-yigit",
                User::class.java)
}