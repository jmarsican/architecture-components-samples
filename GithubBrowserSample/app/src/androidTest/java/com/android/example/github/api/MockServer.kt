package com.android.example.github.api

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object MockServer {
    private val server = MockWebServer()
    fun baseUrl(): HttpUrl = server.url("/")

    fun readTextFile(inputStream: InputStream): String? {
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

    fun openJsonFile(fileName: String) = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName.json")

    fun enqueueErrorResponse() {
        server.enqueue(
                MockResponse().setResponseCode(403)
        )
    }
}