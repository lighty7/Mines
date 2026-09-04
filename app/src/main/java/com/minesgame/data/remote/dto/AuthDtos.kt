package com.minesgame.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("address") val address: String? = null,
)

data class UpdateProfileRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("address") val address: String? = null,
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("address") val address: String? = null,
    @SerializedName("balance") val balance: Double,
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto,
)

data class UserProfileResponse(
    @SerializedName("user") val user: UserDto,
)

data class BalanceResponse(
    @SerializedName("balance") val balance: Double,
)

data class ErrorResponse(
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
)

data class SendOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("reason") val reason: String? = null,
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
    @SerializedName("newPassword") val newPassword: String,
)

data class MessageResponse(
    @SerializedName("message") val message: String,
)

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("roundId") val roundId: String? = null,
    @SerializedName("createdAt") val createdAt: String,
)

data class TransactionsResponse(
    @SerializedName("transactions") val transactions: List<TransactionDto>,
)
