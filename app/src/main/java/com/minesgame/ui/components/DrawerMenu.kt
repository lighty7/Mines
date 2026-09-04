package com.minesgame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minesgame.data.model.UserProfile
import com.minesgame.ui.theme.Green
import com.minesgame.ui.theme.Panel
import com.minesgame.ui.theme.SecondaryText
import com.minesgame.ui.theme.TextPrimary
import com.minesgame.ui.theme.Tile
import com.minesgame.ui.theme.TileBorder

@Composable
fun LeftDrawerContent(
    userProfile: UserProfile,
    selectedLanguage: String,
    hapticsEnabled: Boolean,
    soundEnabled: Boolean,
    onLanguageSelect: (String) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val languages = listOf("English", "Español", "Português", "Hindi")

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Panel)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        // Profile Header Card in Drawer
        Card(
            onClick = onOpenProfile,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Tile),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Green),
                    contentAlignment = Alignment.Center,
                ) {
                    val initial = (userProfile.username.firstOrNull() ?: 'P').uppercaseChar().toString()
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = if (userProfile.email.isNotBlank()) userProfile.email else "Tap to edit profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                    )
                }
            }
        }

        HorizontalDivider(color = TileBorder)

        // Language Section
        Text(
            text = "App Language",
            style = MaterialTheme.typography.titleSmall,
            color = SecondaryText,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            languages.forEach { lang ->
                val isSelected = lang == selectedLanguage
                Surface(
                    onClick = { onLanguageSelect(lang) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Green.copy(alpha = 0.15f) else Tile,
                    border = if (isSelected) BorderStroke(1.dp, Green) else null,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = lang,
                            color = if (isSelected) Green else TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (isSelected) {
                            Text("✓", color = Green, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = TileBorder)

        // Audio & Haptics Section
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.titleSmall,
            color = SecondaryText,
        )

        // Haptic Feedback Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Vibration / Haptics", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text("Tactile feedback on clicks", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = hapticsEnabled,
                onCheckedChange = onHapticsToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Green,
                    uncheckedThumbColor = SecondaryText,
                    uncheckedTrackColor = Tile,
                ),
            )
        }

        // Sound Effects Switch (with future Sound Execution Pseudo-code)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Sound Effects (SFX)", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text("Audio effects for win/loss", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = soundEnabled,
                onCheckedChange = { enabled ->
                    onSoundToggle(enabled)

                    /*
                     * =========================================================================
                     * TODO / PSEUDO-CODE FOR FUTURE SOUND SFX EXECUTION:
                     * =========================================================================
                     * val soundPlayer = SoundPlayer.getInstance(context)
                     * if (enabled) {
                     *     // Play sound effect triggers during gameplay events:
                     *     // - On Tile Reveal Safe: soundPlayer.playSound(R.raw.gem_reveal)
                     *     // - On Tile Reveal Mine: soundPlayer.playSound(R.raw.mine_explode)
                     *     // - On Cash Out Win:     soundPlayer.playSound(R.raw.cash_out_win)
                     * } else {
                     *     // Mute all audio streams
                     *     soundPlayer.mute()
                     * }
                     * =========================================================================
                     */
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Green,
                    uncheckedThumbColor = SecondaryText,
                    uncheckedTrackColor = Tile,
                ),
            )
        }

        HorizontalDivider(color = TileBorder)

        // Game Info & Rules
        Surface(
            onClick = onOpenHowToPlay,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Tile,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📖 How to Play & Rules", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text(">", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlayModal(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "📖 How to Play Mines",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Tile),
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("1. Set your Bet Amount and number of Mines.", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("2. Click BET to start the game round.", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("3. Click tiles on the grid to uncover hidden Gems 💎.", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("4. Every safe pick increases your Win Multiplier (99% RTP).", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("5. Click CASH OUT at any time to take your winnings!", color = Green, style = MaterialTheme.typography.bodyMedium)
                    Text("⚠️ Hitting a Mine 💣 explodes the board and loses the round.", color = Color(0xFFF04444), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
            ) {
                Text("Got It!", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
