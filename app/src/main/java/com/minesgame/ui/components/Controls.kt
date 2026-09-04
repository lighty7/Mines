package com.minesgame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minesgame.data.engine.MinesEngine
import com.minesgame.data.model.GameResult
import com.minesgame.data.model.GameState
import androidx.compose.foundation.shape.CircleShape
import com.minesgame.data.model.UserProfile
import com.minesgame.ui.theme.Green
import com.minesgame.ui.theme.Panel
import com.minesgame.ui.theme.Red
import com.minesgame.ui.theme.SecondaryText
import com.minesgame.ui.theme.TextPrimary
import com.minesgame.ui.theme.Tile
import com.minesgame.ui.theme.TileBorder
import com.minesgame.ui.viewmodel.GameUiState
import java.util.Locale

private fun formatAmount(value: Double): String = String.format(Locale.US, "%.2f", value)

@Composable
fun TopBar(
    balance: String,
    userProfile: UserProfile,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left Actions: Hamburger Menu + Profile Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onOpenDrawer,
                shape = RoundedCornerShape(8.dp),
                color = Tile,
                contentColor = TextPrimary,
                modifier = Modifier.size(if (compact) 32.dp else 38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("☰", style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge)
                }
            }

            Surface(
                onClick = onOpenProfile,
                shape = CircleShape,
                color = Green,
                contentColor = Color.White,
                modifier = Modifier.size(if (compact) 32.dp else 38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = (userProfile.username.firstOrNull() ?: 'P').uppercaseChar().toString()
                    Text(
                        text = initial,
                        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }

        Text(
            text = "MINES",
            style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = Green,
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Balance",
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
            Text(
                text = balance,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }
    }
}

@Composable
fun StatusCard(
    multiplier: String,
    potentialWin: String,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Multiplier",
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = multiplier,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    maxLines = 1,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "Potential Win",
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = potentialWin,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    color = Green,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun MainActionButton(
    gameState: GameState,
    revealedCount: Int,
    canBet: Boolean,
    potentialWin: String,
    onBet: () -> Unit,
    onCashOut: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val active = gameState == GameState.ACTIVE
    val enabled = if (active) revealedCount > 0 else canBet
    val text = when {
        active && revealedCount > 0 -> "CASH OUT  $potentialWin"
        active -> "CASH OUT"
        else -> "BET"
    }

    Button(
        onClick = { if (active) onCashOut() else onBet() },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 48.dp else 54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White,
            disabledContainerColor = Tile.copy(alpha = 0.5f),
            disabledContentColor = SecondaryText,
        ),
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun CompactControlPanel(
    bet: Double,
    balance: Double,
    mines: Int,
    boardSize: Int,
    gameState: GameState,
    revealedCount: Int,
    potentialWin: String,
    onBetChange: (Double) -> Unit,
    onMinesChange: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onBet: () -> Unit,
    onCashOut: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val active = gameState == GameState.ACTIVE
    val canBet = !active && bet > 0 && bet <= balance

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Bet Amount Column
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = "Bet Amount",
                    style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                    color = SecondaryText,
                )
                Spacer(Modifier.height(4.dp))
                BetInputField(
                    bet = bet,
                    enabled = !active,
                    onBetChange = onBetChange,
                    compact = compact,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    QuickBetButton(label = "1/2", onClick = { onBetChange(bet / 2) }, compact = compact)
                    QuickBetButton(label = "2x", onClick = { onBetChange(bet * 2) }, compact = compact)
                    QuickBetButton(label = "MAX", onClick = { onBetChange(balance) }, compact = compact)
                }
            }

            // Mines Column
            Column(modifier = Modifier.weight(0.9f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Mines ($boardSize x $boardSize)",
                        style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                        color = SecondaryText,
                    )
                    Surface(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(6.dp),
                        color = Tile,
                        contentColor = TextPrimary,
                        modifier = Modifier.size(if (compact) 26.dp else 30.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚙", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                CompactMinesStepper(
                    mines = mines,
                    maxMines = MinesEngine.maxMinesForBoard(boardSize),
                    enabled = !active,
                    onMinesChange = onMinesChange,
                    compact = compact,
                )
            }
        }

        MainActionButton(
            gameState = gameState,
            revealedCount = revealedCount,
            canBet = canBet,
            potentialWin = potentialWin,
            onBet = onBet,
            onCashOut = onCashOut,
            compact = compact,
        )
    }
}

@Composable
private fun BetInputField(
    bet: Double,
    enabled: Boolean,
    onBetChange: (Double) -> Unit,
    compact: Boolean = false,
) {
    var text by remember { mutableStateOf(formatAmount(bet)) }

    LaunchedEffect(bet) {
        text = formatAmount(bet)
    }

    fun commit(input: String) {
        val filtered = input.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        text = filtered
        filtered.toDoubleOrNull()?.let(onBetChange)
    }

    OutlinedTextField(
        value = text,
        onValueChange = ::commit,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green,
            unfocusedBorderColor = TileBorder,
            focusedContainerColor = Tile,
            unfocusedContainerColor = Tile,
            cursorColor = Green,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = SecondaryText,
            disabledContainerColor = Tile,
        ),
    )
}

@Composable
private fun RowScope.QuickBetButton(
    label: String,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, TileBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 0.dp),
    ) {
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun CompactMinesStepper(
    mines: Int,
    maxMines: Int,
    enabled: Boolean,
    onMinesChange: (Int) -> Unit,
    compact: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            label = "-",
            enabled = enabled && mines > 1,
            onClick = { onMinesChange(mines - 1) },
            compact = compact,
        )
        Surface(
            modifier = Modifier.weight(1f).height(if (compact) 44.dp else 52.dp),
            shape = RoundedCornerShape(8.dp),
            color = Tile,
            border = BorderStroke(1.dp, TileBorder),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = mines.toString(),
                    textAlign = TextAlign.Center,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
            }
        }
        StepperButton(
            label = "+",
            enabled = enabled && mines < maxMines,
            onClick = { onMinesChange(mines + 1) },
            compact = compact,
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(if (compact) 44.dp else 52.dp),
        shape = RoundedCornerShape(8.dp),
        color = Tile,
        contentColor = if (enabled) TextPrimary else SecondaryText,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = label,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = if (enabled) TextPrimary else SecondaryText,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSettingsModal(
    boardSize: Int,
    enabled: Boolean,
    mineChancePercent: Double,
    safeChancePercent: Double,
    onBoardSizeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Board Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )

            Text(
                text = "Select Grid Size",
                style = MaterialTheme.typography.titleMedium,
                color = SecondaryText,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(4, 5, 6).forEach { size ->
                    val selected = size == boardSize
                    Surface(
                        onClick = { onBoardSizeChange(size) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Green else Tile,
                        contentColor = if (selected) Color.White else TextPrimary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${size}x${size}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Tile),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Probability Stats",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Mine chance: ${"%.2f".format(mineChancePercent)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Red,
                    )
                    Text(
                        text = "Safe chance: ${"%.2f".format(safeChancePercent)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green,
                    )
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Done", color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResultBanner(result: GameResult, modifier: Modifier = Modifier) {
    val text = if (result.won) {
        "You won ${GameUiState.formatMoney(result.payout)}"
    } else {
        "You lost"
    }
    val color = if (result.won) Green else Red
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
        color = color,
    )
}

