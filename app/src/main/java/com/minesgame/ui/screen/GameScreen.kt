package com.minesgame.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minesgame.data.model.GameState
import com.minesgame.ui.components.BetAmountSection
import com.minesgame.ui.components.BetButton
import com.minesgame.ui.components.CashOutButton
import com.minesgame.ui.components.MineSelectorSection
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
    val sectionSpacing = if (compact) 10.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (compact) 12.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        Spacer(Modifier.height(if (compact) 6.dp else 8.dp))
        TopBar(balance = state.formattedBalance, compact = compact)
        StatusCard(
            multiplier = state.formattedMultiplier,
            potentialWin = state.formattedPotentialWin,
            compact = compact,
        )
        MinesGrid(
            tiles = state.tiles,
            onTileClick = onReveal,
            modifier = Modifier.fillMaxWidth(),
            spacing = gridSpacing,
            boardSize = state.boardSize,
        )
        CashOutButton(
            enabled = active && state.revealedCount > 0,
            potentialWin = state.formattedPotentialWin,
            onClick = onCashOut,
            compact = compact,
        )
        BetAmountSection(
            bet = state.bet,
            balance = state.balance,
            enabled = !active,
            onBetChange = onBetChange,
            compact = compact,
        )
        MineSelectorSection(
            boardSize = state.boardSize,
            mines = state.mines,
            enabled = !active,
            onBoardSizeChange = onBoardSizeChange,
            onMinesChange = onMinesChange,
            mineChancePercent = state.mineChancePercent,
            safeChancePercent = state.safeChancePercent,
            compact = compact,
        )
        BetButton(
            enabled = !active && state.bet > 0 && state.bet <= state.balance,
            onClick = onBet,
            compact = compact,
        )
        state.lastResult?.let { result ->
            ResultBanner(result = result)
        }
        Spacer(Modifier.height(if (compact) 24.dp else 100.dp))
    }
}
