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
fun TopBar(balance: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "MINES",
            style = MaterialTheme.typography.headlineMedium,
            color = Green,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
            Text(
                text = balance,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }
    }
}

@Composable
fun StatusCard(multiplier: String, potentialWin: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Multiplier",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = multiplier,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Potential Win",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Text(
                    text = potentialWin,
                    style = MaterialTheme.typography.headlineLarge,
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
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
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
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun BetButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White,
            disabledContainerColor = Tile.copy(alpha = 0.5f),
            disabledContentColor = SecondaryText,
        ),
    ) {
        Text(text = "BET", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun BetAmountSection(
    bet: Double,
    balance: Double,
    enabled: Boolean,
    onBetChange: (Double) -> Unit,
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
            style = MaterialTheme.typography.titleMedium,
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickBetButton(label = "1/2", onClick = { onBetChange(bet / 2) })
            QuickBetButton(label = "2x", onClick = { onBetChange(bet * 2) })
            QuickBetButton(label = "MAX", onClick = { onBetChange(balance) })
        }
    }
}

@Composable
private fun RowScope.QuickBetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TileBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
    ) {
        Text(text = label)
    }
}

@Composable
fun MineSelectorSection(
    mines: Int,
    enabled: Boolean,
    onMinesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Mines",
            style = MaterialTheme.typography.titleMedium,
            color = SecondaryText,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(
                label = "-",
                enabled = enabled && mines > 1,
                onClick = { onMinesChange(mines - 1) }
            )
            Text(
                text = mines.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            StepperButton(
                label = "+",
                enabled = enabled && mines < MinesEngine.MAX_MINES,
                onClick = { onMinesChange(mines + 1) }
            )
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(44.dp),
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
                style = MaterialTheme.typography.titleLarge,
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
