package com.minesgame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.minesgame.data.model.UserProfile
import com.minesgame.ui.theme.Green
import com.minesgame.ui.theme.Panel
import com.minesgame.ui.theme.Red
import com.minesgame.ui.theme.SecondaryText
import com.minesgame.ui.theme.TextPrimary
import com.minesgame.ui.theme.Tile
import com.minesgame.ui.theme.TileBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileModal(
    userProfile: UserProfile,
    formattedBalance: String,
    onSaveProfile: (username: String, email: String, address: String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (username: String, email: String, address: String, password: String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

    // Auth Form States
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    // Edit Profile States (when logged in)
    var editUsername by remember { mutableStateOf(userProfile.username) }
    var editEmail by remember { mutableStateOf(userProfile.email) }
    var editAddress by remember { mutableStateOf(userProfile.address) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (userProfile.isGuest) {
                // GUEST MODE HEADER & AUTH TABS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Tile),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("👤", style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Text(
                            text = "Guest Session",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Sign in or register to save your account",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                        )
                    }
                }

                // Sign In vs Register Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Sign In", "Register / Sign Up").forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Surface(
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Green else Tile,
                            contentColor = if (selected) Color.White else TextPrimary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // SIGN IN FORM
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Email Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Column {
                            Text("Password", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Button(
                            onClick = {
                                if (loginEmail.isNotBlank()) {
                                    onLogin(loginEmail, loginPassword)
                                    onDismiss()
                                }
                            },
                            enabled = loginEmail.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    // REGISTER FORM
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Username", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Column {
                            Text("Email Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Column {
                            Text("Residential Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regAddress,
                                onValueChange = { regAddress = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Column {
                            Text("Password", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Column {
                            Text("Confirm Password", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = { regConfirmPassword = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Green,
                                    unfocusedBorderColor = TileBorder,
                                    focusedContainerColor = Tile,
                                    unfocusedContainerColor = Tile,
                                    cursorColor = Green,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                        }

                        Button(
                            onClick = {
                                if (regEmail.isNotBlank()) {
                                    onRegister(regUsername, regEmail, regAddress, regPassword)
                                    onDismiss()
                                }
                            },
                            enabled = regEmail.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                        ) {
                            Text("Create Account", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue as Guest", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // LOGGED IN PROFILE VIEW
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Green),
                        contentAlignment = Alignment.Center,
                    ) {
                        val initial = (editUsername.firstOrNull() ?: 'P').uppercaseChar().toString()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                        )
                    }
                    Column {
                        Text(
                            text = userProfile.username,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Authenticated Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green,
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Tile),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Account Balance", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                        Text(formattedBalance, color = Green, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Column {
                    Text("Username", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = TileBorder,
                            focusedContainerColor = Tile,
                            unfocusedContainerColor = Tile,
                            cursorColor = Green,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                }

                Column {
                    Text("Email Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = TileBorder,
                            focusedContainerColor = Tile,
                            unfocusedContainerColor = Tile,
                            cursorColor = Green,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                }

                Column {
                    Text("Residential Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = TileBorder,
                            focusedContainerColor = Tile,
                            unfocusedContainerColor = Tile,
                            cursorColor = Green,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                        ),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Red),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                    ) {
                        Text("Sign Out")
                    }

                    Button(
                        onClick = {
                            onSaveProfile(editUsername, editEmail, editAddress)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                    ) {
                        Text("Save")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
