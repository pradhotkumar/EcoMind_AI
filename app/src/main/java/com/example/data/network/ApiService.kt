package com.example.data.network

import com.example.data.model.EcoMindRequest
import com.example.data.model.EcoMindResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// --- Gemini Request/Response Models ---
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiGenerateRequest(val contents: List<GeminiContent>)

data class GeminiCandidateContent(val parts: List<GeminiPart>)
data class GeminiCandidate(val content: GeminiCandidateContent)
data class GeminiGenerateResponse(val candidates: List<GeminiCandidate>)

interface N8nApiService {
    @POST
    suspend fun sendEcoMindRequest(
        @Url webhookUrl: String,
        @Body request: EcoMindRequest
    ): okhttp3.ResponseBody

    @POST
    suspend fun sendRawJsonRequest(
        @Url webhookUrl: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): okhttp3.ResponseBody

    @retrofit2.http.PUT
    suspend fun putRawJson(
        @Url url: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): okhttp3.ResponseBody

    @retrofit2.http.GET
    suspend fun getRawJson(
        @Url url: String
    ): okhttp3.ResponseBody
}

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

object NetworkClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Base URL is generic since we use `@Url` for actual n8n webhooks
    val n8nService: N8nApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://localhost/") // Placeholder, overridden by `@Url`
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(N8nApiService::class.java)
    }

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}
