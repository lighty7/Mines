package com.minesgame.data.repository

import com.minesgame.data.engine.MinesEngine
import com.minesgame.data.remote.api.ApiClient
import com.minesgame.data.remote.dto.CashoutRequest
import com.minesgame.data.remote.dto.RevealRequest
import com.minesgame.data.remote.dto.StartGameRequest
import java.util.UUID

sealed interface StartRoundResult {
    data class Success(val roundId: String, val balance: Double, val serverSeedHash: String?) : StartRoundResult
    data class Error(val message: String) : StartRoundResult
}

sealed interface RevealTileResult {
    data class Safe(val revealedCount: Int, val multiplier: Double, val potentialWin: Double, val balance: Double) : RevealTileResult
    data class Mine(val mineIndices: List<Int>, val balance: Double) : RevealTileResult
    data class Error(val message: String) : RevealTileResult
}

sealed interface CashOutResult {
    data class Success(val multiplier: Double, val payout: Double, val balance: Double) : CashOutResult
    data class Error(val message: String) : CashOutResult
}

interface GameRepository {
    suspend fun startRound(bet: Double, mines: Int, boardSize: Int): StartRoundResult
    suspend fun revealTile(roundId: String, tileIndex: Int): RevealTileResult
    suspend fun cashOut(roundId: String): CashOutResult
}

class RemoteGameRepository(
    private val apiClient: ApiClient,
) : GameRepository {

    override suspend fun startRound(bet: Double, mines: Int, boardSize: Int): StartRoundResult {
        return try {
            val response = apiClient.api.startGame(
                StartGameRequest(
                    bet = bet,
                    mines = mines,
                    boardSize = boardSize,
                    idempotencyKey = UUID.randomUUID().toString(),
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                StartRoundResult.Success(
                    roundId = body.roundId,
                    balance = body.balance,
                    serverSeedHash = body.serverSeedHash,
                )
            } else {
                StartRoundResult.Error(apiClient.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            StartRoundResult.Error(e.localizedMessage ?: "Network error starting game")
        }
    }

    override suspend fun revealTile(roundId: String, tileIndex: Int): RevealTileResult {
        return try {
            val response = apiClient.api.revealTile(
                RevealRequest(roundId = roundId, tileIndex = tileIndex)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.safe) {
                    RevealTileResult.Safe(
                        revealedCount = body.revealed ?: 1,
                        multiplier = body.multiplier ?: 1.0,
                        potentialWin = body.potentialWin ?: 0.0,
                        balance = body.balance,
                    )
                } else {
                    RevealTileResult.Mine(
                        mineIndices = body.mineIndices ?: listOf(tileIndex),
                        balance = body.balance,
                    )
                }
            } else {
                RevealTileResult.Error(apiClient.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            RevealTileResult.Error(e.localizedMessage ?: "Network error revealing tile")
        }
    }

    override suspend fun cashOut(roundId: String): CashOutResult {
        return try {
            val response = apiClient.api.cashout(CashoutRequest(roundId = roundId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                CashOutResult.Success(
                    multiplier = body.multiplier,
                    payout = body.payout,
                    balance = body.balance,
                )
            } else {
                CashOutResult.Error(apiClient.parseErrorMessage(response))
            }
        } catch (e: Exception) {
            CashOutResult.Error(e.localizedMessage ?: "Network error cashing out")
        }
    }
}

/**
 * Local simulation for Guest / Demo mode (offline playable).
 */
class LocalGameRepository(
    var localBalance: Double = 1000.0,
) : GameRepository {

    private var activeMines: Set<Int> = emptySet()
    private var activeBet: Double = 0.0
    private var activeMinesCount: Int = 5
    private var activeBoardSize: Int = MinesEngine.DEFAULT_BOARD_SIZE
    private var revealedSoFar: Int = 0

    override suspend fun startRound(bet: Double, mines: Int, boardSize: Int): StartRoundResult {
        if (bet <= 0 || bet > localBalance) {
            return StartRoundResult.Error("Invalid bet amount")
        }
        activeBet = bet
        activeMinesCount = mines
        activeBoardSize = boardSize
        revealedSoFar = 0
        activeMines = MinesEngine.generateMinePositions(mines, boardSize)
        localBalance -= bet
        return StartRoundResult.Success(
            roundId = UUID.randomUUID().toString(),
            balance = localBalance,
            serverSeedHash = null,
        )
    }

    override suspend fun revealTile(roundId: String, tileIndex: Int): RevealTileResult {
        if (tileIndex in activeMines) {
            return RevealTileResult.Mine(
                mineIndices = activeMines.toList(),
                balance = localBalance,
            )
        }
        revealedSoFar += 1
        val multiplier = MinesEngine.multiplierAt(activeMinesCount, revealedSoFar, activeBoardSize)
        val potentialWin = activeBet * multiplier
        return RevealTileResult.Safe(
            revealedCount = revealedSoFar,
            multiplier = multiplier,
            potentialWin = potentialWin,
            balance = localBalance,
        )
    }

    override suspend fun cashOut(roundId: String): CashOutResult {
        if (revealedSoFar == 0) {
            return CashOutResult.Error("Reveal at least one tile before cashing out")
        }
        val multiplier = MinesEngine.multiplierAt(activeMinesCount, revealedSoFar, activeBoardSize)
        val payout = activeBet * multiplier
        localBalance += payout
        return CashOutResult.Success(
            multiplier = multiplier,
            payout = payout,
            balance = localBalance,
        )
    }
}
