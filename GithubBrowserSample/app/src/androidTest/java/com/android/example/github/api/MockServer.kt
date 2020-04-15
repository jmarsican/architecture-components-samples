package com.android.example.github.api

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio

object MockServer {
    private val server = MockWebServer()
    fun baseUrl(): HttpUrl = server.url("/")

    fun enqueueJsonResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!
                .getResourceAsStream("api-response/$fileName.json")
        val file = Okio.buffer(Okio.source(inputStream))

        server.enqueue(
                MockResponse().apply {
                    for ((key, value) in headers) {
                        addHeader(key, value)
                    }
                    setBody(file.readString(Charsets.UTF_8))
                }
        )
    }

    fun enqueueErrorResponse() {
        server.enqueue(
                MockResponse().setResponseCode(403)
        )
    }
}