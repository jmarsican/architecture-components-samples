package com.javiermarsicano.githubsample

import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

const val MOCKSERVER_PORT = 1080

enum class ServiceEndpoint(val path: String) {
    CONTRIBUTORS("/repos/.*/.*/contributors"),
    USER_REPOS("/users/%s/repos"),
    USERS("/users/%s"),
    SEARCH_REPOS("/search/repositories")
}

object MockServer {
    private var serviceMocker = ClientAndServer(MOCKSERVER_PORT)

    fun mockContributorsServiceReturnsSuccessful(response: String) {
        mockGetServiceReturnsBody(ServiceEndpoint.CONTRIBUTORS.path, response)
    }

    fun mockUserReposServiceReturnsSuccessful(user:String, response: String) {
        mockGetServiceReturnsBody(String.format(ServiceEndpoint.USER_REPOS.path, user), response)
    }

    fun mockUsersServiceReturnsSuccessful(user:String, response: String) {
        mockGetServiceReturnsBody(String.format(ServiceEndpoint.USERS.path, user), response)
    }

    fun mockSearchReposReturnsSuccessful(response: String) {
        mockGetServiceReturnsBody(ServiceEndpoint.SEARCH_REPOS.path, response)
    }

    fun mockSearchReposReturnsError() {
        mockGetServiceReturnsError(ServiceEndpoint.SEARCH_REPOS.path)
    }

    fun clearAll() {
        serviceMocker.reset()
    }

    private fun mockGetServiceReturnsBody(path: String, responseBody: String) {
        serviceMocker.`when`(
                request()
                        .withMethod("GET")
                        .withPath(path)
        ).respond(
                response()
                        .withBody(responseBody)
        )
    }

    private fun mockGetServiceReturnsError(path: String) {
        serviceMocker.`when`(
                request()
                        .withMethod("GET")
                        .withPath(path)
        ).respond(
                response()
                        .withStatusCode(403)
        )
    }
}
