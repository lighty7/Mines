package com.minesgame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.minesgame.data.engine.MinesEngine
import com.minesgame.data.model.GameResult
import com.minesgame.data.model.GameState
import com.minesgame.data.model.Tile
import com.minesgame.data.model.TileState
import com.minesgame.data.repository.GameRepository
import com.minesgame.data.repository.LocalGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class GameUiState(
    val balance: Double = 1000.0,
    val bet: Double = 10.0,
    val mines: Int = 5,
    val gameState: GameState = GameState.IDLE,
    val tiles: List<Tile> = emptyList(),
    val revealedCount: Int = 0,
    val multiplier: Double = 1.0,
    val potentialWin: Double = 0.0,
    val lastResult: GameResult? = null,
) {
    val formattedBalance: String get() = formatMoney(balance)
    val formattedBet: String get() = formatMoney(bet)
    val formattedPotentialWin: String get() = formatMoney(potentialWin)
    val formattedMultiplier: String get() = String.format(Locale.US, "%.2fx", multiplier)

    companion object {
        fun formatMoney(value: Double): String =
            String.format(Locale.US, "$%.2f", value)
    }
}

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val active
        get() = _uiState.value.gameState == GameState.ACTIVE

    fun setBet(bet: Double) {
        if (active) return
        _uiState.update { it.copy(bet = bet.coerceIn(0.0, it.balance)) }
    }

    fun setMines(mines: Int) {
        if (active) return
        _uiState.update { it.copy(mines = mines.coerceIn(1, MinesEngine.MAX_MINES)) }
    }

    fun placeBet() {
        val s = _uiState.value
        if (active || s.bet <= 0 || s.bet > s.balance) return

        val mines = repository.createBoard(s.mines)
        val tiles = (0 until MinesEngine.TILES)
            .map { Tile(index = it, isMine = it in mines) }

        _uiState.update {
            it.copy(
                balance = it.balance - it.bet,
                gameState = GameState.ACTIVE,
                tiles = tiles,
                revealedCount = 0,
                multiplier = 1.0,
                potentialWin = it.bet,
                lastResult = null,
            )
        }
    }

    fun reveal(index: Int) {
        val s = _uiState.value
        if (!active) return
        val tile = s.tiles.getOrNull(index) ?: return
        if (tile.state != TileState.HIDDEN) return

        val tiles = s.tiles.toMutableList()
        if (tile.isMine) {
            tiles[index] = tile.copy(state = TileState.MINE)
            for (i in tiles.indices) {
                if (tiles[i].isMine) tiles[i] = tiles[i].copy(state = TileState.MINE)
            }
            _uiState.update {
                it.copy(
                    tiles = tiles,
                    gameState = GameState.LOST,
                    multiplier = 0.0,
                    potentialWin = 0.0,
                    lastResult = GameResult(won = false, multiplier = 0.0, payout = 0.0),
                )
            }
        } else {
            tiles[index] = tile.copy(state = TileState.SAFE)
            val revealed = s.revealedCount + 1
            val multiplier = MinesEngine.multiplierAt(s.mines, revealed)
            val potentialWin = s.bet * multiplier
            _uiState.update {
                it.copy(
                    tiles = tiles,
                    revealedCount = revealed,
                    multiplier = multiplier,
                    potentialWin = potentialWin,
                )
            }
        }
    }

    fun cashOut() {
        val s = _uiState.value
        if (!active || s.revealedCount == 0) return
        val payout = s.potentialWin
        _uiState.update {
            it.copy(
                balance = it.balance + payout,
                gameState = GameState.WON,
                lastResult = GameResult(won = true, multiplier = it.multiplier, payout = payout),
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { GameViewModel(LocalGameRepository()) }
        }
    }
}
