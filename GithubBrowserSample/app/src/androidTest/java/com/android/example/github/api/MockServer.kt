package com.android.example.github.api

import okhttp3.mockwebserver.MockWebServer

object MockServer {
    val server = MockWebServer()
    fun baseUrl() = server.url("/")
}