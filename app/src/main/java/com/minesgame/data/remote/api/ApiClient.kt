package com.minesgame.data.remote.api

import com.google.gson.Gson
import com.minesgame.data.local.TokenManager
import com.minesgame.data.remote.dto.ErrorResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val tokenManager: TokenManager) {

    private val gson = Gson()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        val token = tokenManager.getToken()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        builder.header("Content-Type", "application/json")
        builder.header("Accept", "application/json")

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: MinesApiService = retrofit.create(MinesApiService::class.java)

    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val parsed = gson.fromJson(errorBody, ErrorResponse::class.java)
                val raw = parsed.error ?: parsed.message ?: "Request failed (${response.code()})"
                cleanErrorMessage(raw)
            } else {
                "Request failed (${response.code()})"
            }
        } catch (e: Exception) {
            "Request failed (${response.code()})"
        }
    }

    private fun cleanErrorMessage(raw: String): String {
        if (raw.contains("fieldErrors") || raw.contains("Invalid request body:")) {
            val fieldMatch = Regex(""""([a-zA-Z0-9_]+)":\s*\["([^"]+)"]""").find(raw)
            if (fieldMatch != null) {
                val field = fieldMatch.groupValues[1]
                val msg = fieldMatch.groupValues[2]
                return "${field.replaceFirstChar { it.uppercase() }}: $msg"
            }
        }
        return raw
    }

    companion object {
        const val BASE_URL = "https://mines-backend-mex2.onrender.com/"
    }
}
