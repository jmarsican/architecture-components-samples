package com.android.example.github.api

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Okio
import java.io.ByteArrayOutputStream
import java.io.IOException

object MockServer {
    private val server = MockWebServer()

    fun baseUrl(): HttpUrl = server.url("/")

    fun init() {
        server.start()
    }

    fun reset() {
        server.shutdown()
    }

    fun enqueueErrorResponse() {
        server.enqueue(
                MockResponse().setResponseCode(403)
        )
    }

    fun enqueueJsonResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = openJsonFile(fileName)
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

    fun enqueueConditionalResponses(responses: ConditionalResponseComposite) {

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl.encodedPath()
                return responses.buildResponse(path) ?:
                throw IllegalArgumentException("Unexpected request: $request")
            }
        }
        server.setDispatcher(dispatcher)
    }

    class ConditionalResponseComposite(private val path: String, fileName: String,
                                       private val next: ConditionalResponseComposite? = null) {
        private val inputStreamFirst = openJsonFile(fileName)
        private val fileFirst = Okio.buffer(Okio.source(inputStreamFirst))

        fun buildResponse(path: String): MockResponse? {
            return if (path == this.path) {
                MockResponse()
                        .setBody(fileFirst.readString(Charsets.UTF_8))
            } else {
                next?.buildResponse(path)
            }
        }
    }

    fun readTextFile(fileName: String): String? {
        val inputStream = openJsonFile(fileName)
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also{ len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
        }
        return outputStream.toString()
    }

    inline fun <reified T> getObjectFromJsonFile(fileName: String, javaClass: Class<T>): T? {
        return Gson().fromJson(
                readTextFile(fileName),
                javaClass
        )
    }

    fun openJsonFile(fileName: String) = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName.json")
}