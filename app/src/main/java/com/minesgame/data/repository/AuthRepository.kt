package com.minesgame.data.repository

import com.minesgame.data.local.TokenManager
import com.minesgame.data.model.UserProfile
import com.minesgame.data.model.UserTransaction
import com.minesgame.data.remote.api.ApiClient
import com.minesgame.data.remote.dto.LoginRequest
import com.minesgame.data.remote.dto.RegisterRequest
import com.minesgame.data.remote.dto.ResetPasswordRequest
import com.minesgame.data.remote.dto.SendOtpRequest
import com.minesgame.data.remote.dto.UpdateProfileRequest
import com.minesgame.data.remote.dto.VerifyOtpRequest

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

    suspend fun sendOtp(email: String, reason: String = "verification"): Result<String> {
        return try {
            val response = apiClient.api.sendOtp(SendOtpRequest(email = email.trim(), reason = reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to send verification code"))
        }
    }

    suspend fun verifyOtp(email: String, code: String): Result<String> {
        return try {
            val response = apiClient.api.verifyOtp(VerifyOtpRequest(email = email.trim(), code = code.trim()))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to verify code"))
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return try {
            val response = apiClient.api.resetPassword(ResetPasswordRequest(email = email.trim(), code = code.trim(), newPassword = newPassword))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to reset password"))
        }
    }

    suspend fun getTransactions(limit: Int = 50): Result<List<UserTransaction>> {
        return try {
            val response = apiClient.api.getTransactions(limit)
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.transactions.map { dto ->
                    UserTransaction(
                        id = dto.id,
                        type = dto.type,
                        amount = dto.amount,
                        roundId = dto.roundId,
                        createdAt = dto.createdAt,
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(apiClient.parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to load transactions"))
        }
    }

    fun logout() {
        tokenManager.clear()
    }

    fun getUserProfile(): UserProfile = tokenManager.getUserProfile()

    fun getCachedBalance(): Double = tokenManager.getBalance()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}
