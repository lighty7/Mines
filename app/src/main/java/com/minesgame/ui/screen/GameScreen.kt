package com.minesgame.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
    onMinesChange: (Int) -> Unit,
    onBet: () -> Unit,
    onReveal: (Int) -> Unit,
    onCashOut: () -> Unit,
) {
    val active = state.gameState == GameState.ACTIVE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        TopBar(balance = state.formattedBalance)
        Spacer(Modifier.height(12.dp))
        StatusCard(
            multiplier = state.formattedMultiplier,
            potentialWin = state.formattedPotentialWin,
        )
        Spacer(Modifier.height(12.dp))
        MinesGrid(tiles = state.tiles, onTileClick = onReveal)
        Spacer(Modifier.height(16.dp))
        CashOutButton(
            enabled = active && state.revealedCount > 0,
            potentialWin = state.formattedPotentialWin,
            onClick = onCashOut,
        )
        Spacer(Modifier.height(16.dp))
        BetAmountSection(
            bet = state.bet,
            balance = state.balance,
            enabled = !active,
            onBetChange = onBetChange,
        )
        Spacer(Modifier.height(16.dp))
        MineSelectorSection(
            mines = state.mines,
            enabled = !active,
            onMinesChange = onMinesChange,
        )
        Spacer(Modifier.height(16.dp))
        BetButton(
            enabled = !active && state.bet > 0 && state.bet <= state.balance,
            onClick = onBet,
        )
        state.lastResult?.let { result ->
            Spacer(Modifier.height(12.dp))
            ResultBanner(result = result)
        }
        Spacer(Modifier.height(100.dp))
    }
}
