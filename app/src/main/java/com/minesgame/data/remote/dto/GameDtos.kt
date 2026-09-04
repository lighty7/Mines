package com.minesgame.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StartGameRequest(
    @SerializedName("bet") val bet: Double,
    @SerializedName("mines") val mines: Int,
    @SerializedName("boardSize") val boardSize: Int,
    @SerializedName("clientSeed") val clientSeed: String? = null,
    @SerializedName("idempotencyKey") val idempotencyKey: String? = null,
)

data class StartGameResponse(
    @SerializedName("roundId") val roundId: String,
    @SerializedName("boardSize") val boardSize: Int,
    @SerializedName("mines") val mines: Int,
    @SerializedName("status") val status: String,
    @SerializedName("balance") val balance: Double,
    @SerializedName("serverSeedHash") val serverSeedHash: String? = null,
)

data class RevealRequest(
    @SerializedName("roundId") val roundId: String,
    @SerializedName("tileIndex") val tileIndex: Int,
)

data class RevealResponse(
    @SerializedName("safe") val safe: Boolean,
    @SerializedName("status") val status: String? = null,
    @SerializedName("revealed") val revealed: Int? = null,
    @SerializedName("multiplier") val multiplier: Double? = null,
    @SerializedName("potentialWin") val potentialWin: Double? = null,
    @SerializedName("mineIndices") val mineIndices: List<Int>? = null,
    @SerializedName("balance") val balance: Double,
)

data class CashoutRequest(
    @SerializedName("roundId") val roundId: String,
)

data class CashoutResponse(
    @SerializedName("status") val status: String,
    @SerializedName("multiplier") val multiplier: Double,
    @SerializedName("payout") val payout: Double,
    @SerializedName("balance") val balance: Double,
)

data class HealthResponse(
    @SerializedName("status") val status: String,
)
