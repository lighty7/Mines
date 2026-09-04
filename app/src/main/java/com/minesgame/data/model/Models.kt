package com.minesgame.data.model

enum class GameState { IDLE, ACTIVE, WON, LOST }

enum class TileState { HIDDEN, REVEALING, SAFE, MINE }

data class Tile(
    val index: Int,
    val isMine: Boolean,
    val state: TileState = TileState.HIDDEN,
)

data class GameResult(
    val won: Boolean,
    val multiplier: Double,
    val payout: Double,
)

data class UserProfile(
    val username: String = "Guest",
    val email: String = "",
    val address: String = "",
    val isGuest: Boolean = true,
)

