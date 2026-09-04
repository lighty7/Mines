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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minesgame.data.model.GameState
import com.minesgame.ui.components.BoardSettingsModal
import com.minesgame.ui.components.CompactControlPanel
import com.minesgame.ui.components.HowToPlayModal
import com.minesgame.ui.components.LeftDrawerContent
import com.minesgame.ui.components.MinesGrid
import com.minesgame.ui.components.ProfileModal
import com.minesgame.ui.components.ResultBanner
import com.minesgame.ui.components.StatusCard
import com.minesgame.ui.components.TopBar
import com.minesgame.ui.theme.Background
import com.minesgame.ui.theme.Panel
import com.minesgame.ui.viewmodel.GameUiState
import com.minesgame.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

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
        onSaveProfile = viewModel::updateUserProfile,
        onLanguageSelect = viewModel::setLanguage,
        onHapticsToggle = viewModel::setHapticsEnabled,
        onSoundToggle = viewModel::setSoundEnabled,
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
    onSaveProfile: (username: String, email: String, address: String) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
) {
    val active = state.gameState == GameState.ACTIVE
    val configuration = LocalConfiguration.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val compact = configuration.screenWidthDp <= 360 || configuration.screenHeightDp <= 640
    val gridSpacing = if (compact) 4.dp else 6.dp

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showHowToPlaySheet by remember { mutableStateOf(false) }

    fun performHaptic() {
        if (state.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

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

    if (showProfileSheet) {
        ProfileModal(
            userProfile = state.userProfile,
            formattedBalance = state.formattedBalance,
            onSaveProfile = onSaveProfile,
            onDismiss = { showProfileSheet = false },
        )
    }

    if (showHowToPlaySheet) {
        HowToPlayModal(onDismiss = { showHowToPlaySheet = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Panel) {
                LeftDrawerContent(
                    userProfile = state.userProfile,
                    selectedLanguage = state.selectedLanguage,
                    hapticsEnabled = state.hapticsEnabled,
                    soundEnabled = state.soundEnabled,
                    onLanguageSelect = onLanguageSelect,
                    onHapticsToggle = onHapticsToggle,
                    onSoundToggle = onSoundToggle,
                    onOpenProfile = {
                        scope.launch { drawerState.close() }
                        showProfileSheet = true
                    },
                    onOpenHowToPlay = {
                        scope.launch { drawerState.close() }
                        showHowToPlaySheet = true
                    },
                )
            }
        },
    ) {
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
                TopBar(
                    balance = state.formattedBalance,
                    userProfile = state.userProfile,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenProfile = { showProfileSheet = true },
                    compact = compact,
                )
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
                        onTileClick = { index ->
                            performHaptic()
                            onReveal(index)
                        },
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
                onBet = {
                    performHaptic()
                    onBet()
                },
                onCashOut = {
                    performHaptic()
                    onCashOut()
                },
                compact = compact,
            )
        }
    }
}


