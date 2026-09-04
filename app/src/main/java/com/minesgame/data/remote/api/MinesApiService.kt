package com.minesgame.data.remote.api

import com.minesgame.data.remote.dto.AuthResponse
import com.minesgame.data.remote.dto.BalanceResponse
import com.minesgame.data.remote.dto.CashoutRequest
import com.minesgame.data.remote.dto.CashoutResponse
import com.minesgame.data.remote.dto.HealthResponse
import com.minesgame.data.remote.dto.LoginRequest
import com.minesgame.data.remote.dto.RegisterRequest
import com.minesgame.data.remote.dto.RevealRequest
import com.minesgame.data.remote.dto.RevealResponse
import com.minesgame.data.remote.dto.StartGameRequest
import com.minesgame.data.remote.dto.StartGameResponse
import com.minesgame.data.remote.dto.UpdateProfileRequest
import com.minesgame.data.remote.dto.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface MinesApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserProfileResponse>

    @GET("api/user/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @POST("api/game/start")
    suspend fun startGame(@Body request: StartGameRequest): Response<StartGameResponse>

    @POST("api/game/reveal")
    suspend fun revealTile(@Body request: RevealRequest): Response<RevealResponse>

    @POST("api/game/cashout")
    suspend fun cashout(@Body request: CashoutRequest): Response<CashoutResponse>
}
