package com.minesgame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
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
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
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
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
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
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Multiplier",
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = multiplier,
                    style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Potential Win",
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = potentialWin,
                    style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    color = Green,
                )
            }
        }
    }
}

@Composable
fun CashOutButton(
    enabled: Boolean,
    potentialWin: String,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 48.dp else 56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White,
            disabledContainerColor = Tile,
            disabledContentColor = SecondaryText,
        ),
    ) {
        Text(
            text = if (enabled) "CASH OUT  $potentialWin" else "CASH OUT",
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun BetButton(
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 52.dp else 64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White,
            disabledContainerColor = Tile.copy(alpha = 0.5f),
            disabledContentColor = SecondaryText,
        ),
    ) {
        Text(
            text = "BET",
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun BetAmountSection(
    bet: Double,
    balance: Double,
    enabled: Boolean,
    onBetChange: (Double) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
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

    Column(modifier = modifier) {
        Text(
            text = "Bet Amount",
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = SecondaryText,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = text,
            onValueChange = ::commit,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = if (compact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyLarge,
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
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)) {
            QuickBetButton(label = "1/2", onClick = { onBetChange(bet / 2) }, compact = compact)
            QuickBetButton(label = "2x", onClick = { onBetChange(bet * 2) }, compact = compact)
            QuickBetButton(label = "MAX", onClick = { onBetChange(balance) }, compact = compact)
        }
    }
}

@Composable
private fun RowScope.QuickBetButton(
    label: String,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TileBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
    ) {
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun MineSelectorSection(
    boardSize: Int,
    mines: Int,
    enabled: Boolean,
    onBoardSizeChange: (Int) -> Unit,
    onMinesChange: (Int) -> Unit,
    mineChancePercent: Double,
    safeChancePercent: Double,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Board size",
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = SecondaryText,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            listOf(4, 5, 6).forEach { size ->
                val selected = size == boardSize
                Surface(
                    onClick = { onBoardSizeChange(size) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Green else Tile,
                    contentColor = if (selected) Color.White else TextPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${size}x${size}",
                            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Mine chance: ${"%.2f".format(mineChancePercent)}%  •  Safe chance: ${"%.2f".format(safeChancePercent)}%",
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Mines",
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = SecondaryText,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(
                label = "-",
                enabled = enabled && mines > 1,
                onClick = { onMinesChange(mines - 1) },
                compact = compact,
            )
            Text(
                text = mines.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            StepperButton(
                label = "+",
                enabled = enabled && mines < MinesEngine.maxMinesForBoard(boardSize),
                onClick = { onMinesChange(mines + 1) },
                compact = compact,
            )
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(if (compact) 38.dp else 44.dp),
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
