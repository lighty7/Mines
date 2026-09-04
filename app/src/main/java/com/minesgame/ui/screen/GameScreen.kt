package com.minesgame.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minesgame.data.model.GameState
import com.minesgame.ui.components.BoardSettingsModal
import com.minesgame.ui.components.CompactControlPanel
import com.minesgame.ui.components.MinesGrid
import com.minesgame.ui.components.ResultBanner
import com.minesgame.ui.components.StatusCard
import com.minesgame.ui.components.TopBar
import com.minesgame.ui.theme.Background
import com.minesgame.ui.viewmodel.GameUiState
import com.minesgame.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()

    MinesGameContent(
        state = state,
        onBetChange = viewModel::setBet,
        onBoardSizeChange = viewModel::setBoardSize,
        onMinesChange = viewModel::setMines,
        onBet = viewModel::placeBet,
        onReveal = viewModel::reveal,
        onCashOut = viewModel::cashOut,
    )
}

@Composable
private fun MinesGameContent(
    state: GameUiState,
    onBetChange: (Double) -> Unit,
    onBoardSizeChange: (Int) -> Unit,
    onMinesChange: (Int) -> Unit,
    onBet: () -> Unit,
    onReveal: (Int) -> Unit,
    onCashOut: () -> Unit,
) {
    val active = state.gameState == GameState.ACTIVE
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val compact = screenWidth <= 360 || screenHeight <= 640
    val gridSpacing = if (compact) 4.dp else 6.dp
    var showSettingsSheet by remember { mutableStateOf(false) }

    if (showSettingsSheet) {
        BoardSettingsModal(
            boardSize = state.boardSize,
            enabled = !active,
            mineChancePercent = state.mineChancePercent,
            safeChancePercent = state.safeChancePercent,
            onBoardSizeChange = onBoardSizeChange,
            onDismiss = { showSettingsSheet = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .padding(horizontal = if (compact) 12.dp else 16.dp, vertical = if (compact) 8.dp else 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Bar & Status Header
        Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp)) {
            TopBar(balance = state.formattedBalance, compact = compact)
            StatusCard(
                multiplier = state.formattedMultiplier,
                potentialWin = state.formattedPotentialWin,
                compact = compact,
            )
        }

        // Center Area: Result Banner & Mines Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
            ) {
                state.lastResult?.let { result ->
                    ResultBanner(result = result)
                }
                MinesGrid(
                    tiles = state.tiles,
                    onTileClick = onReveal,
                    modifier = Modifier.fillMaxWidth(),
                    spacing = gridSpacing,
                    boardSize = state.boardSize,
                )
            }
        }

        Spacer(Modifier.height(if (compact) 6.dp else 10.dp))

        // Bottom Controls
        CompactControlPanel(
            bet = state.bet,
            balance = state.balance,
            mines = state.mines,
            boardSize = state.boardSize,
            gameState = state.gameState,
            revealedCount = state.revealedCount,
            potentialWin = state.formattedPotentialWin,
            onBetChange = onBetChange,
            onMinesChange = onMinesChange,
            onOpenSettings = { showSettingsSheet = true },
            onBet = onBet,
            onCashOut = onCashOut,
            compact = compact,
        )
    }
}

