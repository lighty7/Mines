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

data class UserTransaction(
    val id: String,
    val type: String, // BET, WIN, DEPOSIT, WITHDRAW
    val amount: Double,
    val roundId: String? = null,
    val createdAt: String,
) {
    val formattedAmount: String
        get() = String.format(java.util.Locale.US, "%.2f mineCoin", amount)

    val formattedDateTime: String
        get() {
            return try {
                val clean = createdAt.replace("Z", "+0000")
                val pattern = if (clean.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSSZ" else "yyyy-MM-dd'T'HH:mm:ssZ"
                val inputFormat = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
                val date = inputFormat.parse(clean) ?: java.util.Date()
                val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                outputFormat.format(date)
            } catch (_: Exception) {
                createdAt.take(19).replace("T", " ")
            }
        }
}

