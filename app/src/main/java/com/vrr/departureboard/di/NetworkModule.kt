package com.vrr.departureboard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.ContentType
import kotlinx.serialization.json.Json
import android.util.Log
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json, ContentType.Application.Json)
            json(json, ContentType.Text.Html) // VRR API returns JSON with text/html content type
            json(json, ContentType.Text.Plain)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("VrrApi", message)
                }
            }
            level = LogLevel.ALL
        }
        engine {
            connectTimeout = 15_000
            socketTimeout = 15_000
        }
    }
}
