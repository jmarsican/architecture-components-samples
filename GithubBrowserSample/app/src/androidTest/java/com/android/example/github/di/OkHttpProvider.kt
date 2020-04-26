package com.android.example.github.di

import okhttp3.OkHttpClient

object OkHttpProvider {

    val instance: OkHttpClient = OkHttpClient.Builder().build()
}