package com.minesgame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.minesgame.data.engine.MinesEngine
import com.minesgame.data.local.TokenManager
import com.minesgame.data.model.GameResult
import com.minesgame.data.model.GameState
import com.minesgame.data.model.Tile
import com.minesgame.data.model.TileState
import com.minesgame.data.model.UserProfile
import com.minesgame.data.model.UserTransaction
import com.minesgame.data.remote.api.ApiClient
import com.minesgame.data.repository.AuthRepository
import com.minesgame.data.repository.CashOutResult
import com.minesgame.data.repository.GameRepository
import com.minesgame.data.repository.LocalGameRepository
import com.minesgame.data.repository.RemoteGameRepository
import com.minesgame.data.repository.RevealTileResult
import com.minesgame.data.repository.StartRoundResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class GameUiState(
    val balance: Double = 1000.0,
    val bet: Double = 10.0,
    val boardSize: Int = MinesEngine.DEFAULT_BOARD_SIZE,
    val mines: Int = 5,
    val gameState: GameState = GameState.IDLE,
    val tiles: List<Tile> = emptyList(),
    val revealedCount: Int = 0,
    val multiplier: Double = 1.0,
    val potentialWin: Double = 0.0,
    val lastResult: GameResult? = null,
    val userProfile: UserProfile = UserProfile(),
    val selectedLanguage: String = "English",
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val activeRoundId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val serverOnline: Boolean? = null,
    val transactions: List<UserTransaction> = emptyList(),
    val isTransactionsLoading: Boolean = false,
) {
    val formattedBalance: String get() = formatMoney(balance)
    val formattedBet: String get() = formatMoney(bet)
    val formattedPotentialWin: String get() = formatMoney(potentialWin)
    val formattedMultiplier: String get() = String.format(Locale.US, "%.2fx", multiplier)
    val mineChancePercent: Double get() = MinesEngine.mineChancePercentage(boardSize, mines)
    val safeChancePercent: Double get() = MinesEngine.safeChancePercentage(boardSize, mines)

    companion object {
        fun formatMoney(value: Double): String =
            String.format(Locale.US, "%.2f mineCoin", value)
    }
}

class GameViewModel(
    private val authRepository: AuthRepository,
    private val remoteRepository: GameRepository,
    private val localRepository: GameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val active
        get() = _uiState.value.gameState == GameState.ACTIVE

    private val currentRepository: GameRepository
        get() = if (_uiState.value.userProfile.isGuest) localRepository else remoteRepository

    init {
        val cachedProfile = authRepository.getUserProfile()
        val cachedBalance = if (!cachedProfile.isGuest) authRepository.getCachedBalance() else 1000.0
        _uiState.update {
            it.copy(
                userProfile = cachedProfile,
                balance = cachedBalance,
            )
        }

        viewModelScope.launch {
            val online = authRepository.checkHealth()
            _uiState.update { it.copy(serverOnline = online) }

            if (authRepository.isLoggedIn()) {
                authRepository.refreshProfileAndBalance().onSuccess { (profile, balance) ->
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            balance = balance,
                        )
                    }
                    loadTransactions()
                }
            }
        }
    }

    fun setBet(bet: Double) {
        if (active || _uiState.value.isLoading) return
        _uiState.update { it.copy(bet = bet.coerceIn(0.0, it.balance)) }
    }

    fun setBoardSize(boardSize: Int) {
        if (active || _uiState.value.isLoading) return
        val normalized = boardSize.coerceIn(MinesEngine.MIN_BOARD_SIZE, MinesEngine.MAX_BOARD_SIZE)
        _uiState.update {
            val maxMines = MinesEngine.maxMinesForBoard(normalized)
            it.copy(
                boardSize = normalized,
                mines = it.mines.coerceIn(1, maxMines),
            )
        }
    }

    fun setMines(mines: Int) {
        if (active || _uiState.value.isLoading) return
        _uiState.update {
            it.copy(mines = mines.coerceIn(1, MinesEngine.maxMinesForBoard(it.boardSize)))
        }
    }

    fun placeBet() {
        val s = _uiState.value
        if (active || s.isLoading || s.bet <= 0 || s.bet > s.balance) return

        val total = MinesEngine.totalTiles(s.boardSize)
        val initialTiles = (0 until total).map { Tile(index = it, isMine = false, state = TileState.HIDDEN) }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = currentRepository.startRound(s.bet, s.mines, s.boardSize)) {
                is StartRoundResult.Success -> {
                    _uiState.update {
                        it.copy(
                            balance = result.balance,
                            gameState = GameState.ACTIVE,
                            activeRoundId = result.roundId,
                            tiles = initialTiles,
                            revealedCount = 0,
                            multiplier = 1.0,
                            potentialWin = s.bet,
                            lastResult = null,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                    loadTransactions()
                }
                is StartRoundResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun reveal(index: Int) {
        val s = _uiState.value
        if (!active || s.isLoading) return
        val roundId = s.activeRoundId ?: return
        val tile = s.tiles.getOrNull(index) ?: return
        if (tile.state != TileState.HIDDEN) return

        val tiles = s.tiles.toMutableList()
        tiles[index] = tile.copy(state = TileState.REVEALING)
        _uiState.update { it.copy(tiles = tiles, isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = currentRepository.revealTile(roundId, index)) {
                is RevealTileResult.Safe -> {
                    val updated = _uiState.value.tiles.toMutableList()
                    updated[index] = Tile(index = index, isMine = false, state = TileState.SAFE)
                    _uiState.update {
                        it.copy(
                            tiles = updated,
                            revealedCount = result.revealedCount,
                            multiplier = result.multiplier,
                            potentialWin = result.potentialWin,
                            balance = result.balance,
                            isLoading = false,
                        )
                    }
                }
                is RevealTileResult.Mine -> {
                    val updated = _uiState.value.tiles.toMutableList()
                    for (i in updated.indices) {
                        if (i in result.mineIndices || i == index) {
                            updated[i] = Tile(index = i, isMine = true, state = TileState.MINE)
                        }
                    }
                    _uiState.update {
                        it.copy(
                            tiles = updated,
                            gameState = GameState.LOST,
                            multiplier = 0.0,
                            potentialWin = 0.0,
                            balance = result.balance,
                            lastResult = GameResult(won = false, multiplier = 0.0, payout = 0.0),
                            activeRoundId = null,
                            isLoading = false,
                        )
                    }
                    loadTransactions()
                }
                is RevealTileResult.Error -> {
                    val updated = _uiState.value.tiles.toMutableList()
                    updated[index] = tile.copy(state = TileState.HIDDEN)
                    _uiState.update {
                        it.copy(
                            tiles = updated,
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun cashOut() {
        val s = _uiState.value
        if (!active || s.isLoading || s.revealedCount == 0) return
        val roundId = s.activeRoundId ?: return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = currentRepository.cashOut(roundId)) {
                is CashOutResult.Success -> {
                    _uiState.update {
                        it.copy(
                            balance = result.balance,
                            gameState = GameState.WON,
                            multiplier = result.multiplier,
                            lastResult = GameResult(won = true, multiplier = result.multiplier, payout = result.payout),
                            activeRoundId = null,
                            isLoading = false,
                        )
                    }
                    loadTransactions()
                }
                is CashOutResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { (profile, balance) ->
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            balance = balance,
                            isLoading = false,
                            errorMessage = null,
                            gameState = GameState.IDLE,
                            activeRoundId = null,
                            tiles = emptyList(),
                        )
                    }
                    loadTransactions()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Sign in failed",
                        )
                    }
                }
        }
    }

    fun register(username: String, email: String, address: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.register(username, email, address, password)
                .onSuccess { (profile, balance) ->
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            balance = balance,
                            isLoading = false,
                            errorMessage = null,
                            gameState = GameState.IDLE,
                            activeRoundId = null,
                            tiles = emptyList(),
                        )
                    }
                    loadTransactions()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Registration failed",
                        )
                    }
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            it.copy(
                userProfile = UserProfile(username = "Guest", isGuest = true),
                balance = 1000.0,
                gameState = GameState.IDLE,
                activeRoundId = null,
                tiles = emptyList(),
                transactions = emptyList(),
                errorMessage = null,
            )
        }
    }

    fun loadTransactions() {
        if (_uiState.value.userProfile.isGuest) return
        _uiState.update { it.copy(isTransactionsLoading = true) }
        viewModelScope.launch {
            authRepository.getTransactions()
                .onSuccess { list ->
                    _uiState.update { it.copy(transactions = list, isTransactionsLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isTransactionsLoading = false) }
                }
        }
    }

    suspend fun sendOtp(email: String, reason: String = "verification"): Result<String> {
        return authRepository.sendOtp(email, reason)
    }

    suspend fun verifyOtp(email: String, code: String): Result<String> {
        return authRepository.verifyOtp(email, code)
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return authRepository.resetPassword(email, code, newPassword)
    }

    fun updateUserProfile(username: String, email: String, address: String) {
        viewModelScope.launch {
            if (!_uiState.value.userProfile.isGuest) {
                authRepository.updateProfile(username, address).onSuccess { profile ->
                    _uiState.update { it.copy(userProfile = profile) }
                }
            } else {
                _uiState.update {
                    it.copy(
                        userProfile = it.userProfile.copy(
                            username = username.ifBlank { "Guest" },
                            email = email,
                            address = address,
                        )
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application context not found")
                val context = app.applicationContext
                val tokenManager = TokenManager(context)
                val apiClient = ApiClient(tokenManager)
                val authRepository = AuthRepository(apiClient, tokenManager)
                val remoteRepository = RemoteGameRepository(apiClient)
                val localRepository = LocalGameRepository()

                GameViewModel(
                    authRepository = authRepository,
                    remoteRepository = remoteRepository,
                    localRepository = localRepository,
                )
            }
        }
    }
}
