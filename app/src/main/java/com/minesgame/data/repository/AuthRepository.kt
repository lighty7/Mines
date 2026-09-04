package com.minesgame.data.repository

import com.minesgame.data.local.TokenManager
import com.minesgame.data.model.UserProfile
import com.minesgame.data.remote.api.ApiClient
import com.minesgame.data.remote.dto.LoginRequest
import com.minesgame.data.remote.dto.RegisterRequest
import com.minesgame.data.remote.dto.UpdateProfileRequest

class AuthRepository(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager,
) {

    suspend fun checkHealth(): Boolean {
        return try {
            val response = apiClient.api.checkHealth()
            response.isSuccessful && response.body()?.status == "ok"
        } catch (_: Exception) {
            false
        }
    }

    suspend fun login(email: String, password: String): Result<Pair<UserProfile, Double>> {
        return try {
            val response = apiClient.api.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveAuth(body.token, body.user)
                val profile = UserProfile(
                    username = body.user.username,
                    email = body.user.email,
                    address = body.user.address ?: "",
                    isGuest = false,
                )
                Result.success(Pair(profile, body.user.balance))
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to connect to server"))
        }
    }

    suspend fun register(
        username: String,
        email: String,
        address: String,
        password: String,
    ): Result<Pair<UserProfile, Double>> {
        return try {
            val response = apiClient.api.register(
                RegisterRequest(
                    username = username.trim(),
                    email = email.trim(),
                    password = password,
                    address = address.trim().ifBlank { null },
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveAuth(body.token, body.user)
                val profile = UserProfile(
                    username = body.user.username,
                    email = body.user.email,
                    address = body.user.address ?: "",
                    isGuest = false,
                )
                Result.success(Pair(profile, body.user.balance))
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to connect to server"))
        }
    }

    suspend fun refreshProfileAndBalance(): Result<Pair<UserProfile, Double>> {
        if (!tokenManager.isLoggedIn()) {
            return Result.failure(Exception("Not logged in"))
        }
        return try {
            val response = apiClient.api.getMe()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                tokenManager.updateProfile(user.username, user.address)
                tokenManager.updateBalance(user.balance)
                val profile = UserProfile(
                    username = user.username,
                    email = user.email,
                    address = user.address ?: "",
                    isGuest = false,
                )
                Result.success(Pair(profile, user.balance))
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to fetch profile"))
        }
    }

    suspend fun updateProfile(username: String, address: String): Result<UserProfile> {
        return try {
            val response = apiClient.api.updateProfile(
                UpdateProfileRequest(
                    username = username.trim().ifBlank { null },
                    address = address.trim().ifBlank { null },
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                tokenManager.updateProfile(user.username, user.address)
                Result.success(
                    UserProfile(
                        username = user.username,
                        email = user.email,
                        address = user.address ?: "",
                        isGuest = false,
                    )
                )
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update profile"))
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    fun getUserProfile(): UserProfile = tokenManager.getUserProfile()

    fun getCachedBalance(): Double = tokenManager.getBalance()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}
