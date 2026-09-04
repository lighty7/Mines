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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minesgame.data.model.UserProfile
import com.minesgame.data.model.UserTransaction
import com.minesgame.ui.theme.Cyan
import com.minesgame.ui.theme.Gold
import com.minesgame.ui.theme.Green
import com.minesgame.ui.theme.Panel
import com.minesgame.ui.theme.Red
import com.minesgame.ui.theme.SecondaryText
import com.minesgame.ui.theme.TextPrimary
import com.minesgame.ui.theme.Tile
import com.minesgame.ui.theme.TileBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileModal(
    userProfile: UserProfile,
    formattedBalance: String,
    transactions: List<UserTransaction> = emptyList(),
    isTransactionsLoading: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSaveProfile: (username: String, email: String, address: String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (username: String, email: String, address: String, password: String) -> Unit,
    onLogout: () -> Unit,
    onRefreshTransactions: () -> Unit = {},
    onSendOtp: suspend (email: String, reason: String) -> Result<String>,
    onVerifyOtp: suspend (email: String, code: String) -> Result<String>,
    onResetPassword: suspend (email: String, code: String, newPassword: String) -> Result<String>,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Prevent bottom sheet from collapsing / dropping down to partial height when keyboard appears
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // Tabs: For Guest (0: Sign In, 1: Register); For Logged In (0: Details, 1: Transactions)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Sign In Form States
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Forgot Password States
    var isForgotPassword by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotOtpCode by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotConfirmPassword by remember { mutableStateOf("") }
    var forgotStep by remember { mutableIntStateOf(0) } // 0: enter email, 1: enter otp + new password
    var forgotResendTimer by remember { mutableIntStateOf(0) }

    // Register Form States
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regOtpStep by remember { mutableStateOf(false) }
    var regOtpCode by remember { mutableStateOf("") }
    var regResendTimer by remember { mutableIntStateOf(0) }

    // Logged In Edit Profile States
    var editUsername by remember(userProfile.username) { mutableStateOf(userProfile.username) }
    var editEmail by remember(userProfile.email) { mutableStateOf(userProfile.email) }
    var editAddress by remember(userProfile.address) { mutableStateOf(userProfile.address) }

    // Local feedback
    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }
    var isActionBusy by remember { mutableStateOf(false) }

    // Countdown timers for OTP resend
    LaunchedEffect(forgotResendTimer) {
        if (forgotResendTimer > 0) {
            delay(1000L)
            forgotResendTimer -= 1
        }
    }

    LaunchedEffect(regResendTimer) {
        if (regResendTimer > 0) {
            delay(1000L)
            regResendTimer -= 1
        }
    }

    // Refresh transactions when opening logged-in profile or switching to transactions tab
    LaunchedEffect(userProfile.isGuest, selectedTab) {
        if (!userProfile.isGuest && selectedTab == 1) {
            onRefreshTransactions()
        }
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Panel,
        contentColor = TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (userProfile.isGuest) {
                // ==========================================
                // GUEST / AUTHENTICATION VIEW
                // ==========================================
                if (!isForgotPassword) {
                    // Header
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
                                text = "Sign in or register to sync your balance and history",
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
                                onClick = {
                                    selectedTab = index
                                    localError = null
                                    localSuccess = null
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) Green else Tile,
                                contentColor = if (selected) Color.White else TextPrimary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }

                    // Success Feedback Banner
                    if (!localSuccess.isNullOrBlank()) {
                        Surface(
                            color = Green.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Green.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "✓ $localSuccess",
                                color = Green,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    // Error Feedback Banner
                    val displayError = localError ?: errorMessage
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            color = Red.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = displayError,
                                color = Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        // ------------------------------------
                        // SIGN IN FORM
                        // ------------------------------------
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column {
                                Text("Email Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    ),
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
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            if (loginEmail.isNotBlank() && loginPassword.isNotBlank()) {
                                                localError = null
                                                onLogin(loginEmail, loginPassword)
                                            }
                                        },
                                    ),
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
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        forgotEmail = loginEmail
                                        forgotStep = 0
                                        localError = null
                                        localSuccess = null
                                        isForgotPassword = true
                                        focusManager.clearFocus()
                                    },
                                ) {
                                    Text("Forgot Password?", color = Cyan, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    if (loginEmail.isNotBlank() && loginPassword.isNotBlank()) {
                                        localError = null
                                        onLogin(loginEmail, loginPassword)
                                    }
                                },
                                enabled = loginEmail.isNotBlank() && loginPassword.isNotBlank() && !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Sign In", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    } else {
                        // ------------------------------------
                        // REGISTER FORM (WITH OTP VERIFY)
                        // ------------------------------------
                        if (!regOtpStep) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text("Username", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                        Text("3–24 chars, no spaces", color = SecondaryText.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = regUsername,
                                        onValueChange = { input ->
                                            regUsername = input.filter { it.isLetterOrDigit() || it == '_' }.take(24)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        placeholder = { Text("e.g. Player_123", color = SecondaryText.copy(alpha = 0.5f)) },
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        ),
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
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        ),
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
                                    Text("Residential Address (Optional)", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = regAddress,
                                        onValueChange = { regAddress = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Next,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        ),
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
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Next,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        ),
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
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { keyboardController?.hide() },
                                        ),
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

                                val isUsernameValid = regUsername.length in 3..24
                                val isEmailValid = regEmail.contains("@") && regEmail.contains(".")
                                val isPasswordLongEnough = regPassword.length >= 6
                                val passwordsMatch = regPassword.isNotBlank() && regPassword == regConfirmPassword
                                val canSendCode = isUsernameValid && isEmailValid && passwordsMatch && isPasswordLongEnough && !isActionBusy

                                if (regUsername.isNotEmpty() && regUsername.length < 3) {
                                    Text("Username must be at least 3 characters", color = Red, style = MaterialTheme.typography.bodySmall)
                                }
                                if (regPassword.isNotEmpty() && regPassword.length < 6) {
                                    Text("Password must be at least 6 characters", color = Red, style = MaterialTheme.typography.bodySmall)
                                }
                                if (regPassword.isNotBlank() && regConfirmPassword.isNotBlank() && regPassword != regConfirmPassword) {
                                    Text("Passwords do not match", color = Red, style = MaterialTheme.typography.bodySmall)
                                }

                                Button(
                                    onClick = {
                                        if (canSendCode) {
                                            keyboardController?.hide()
                                            isActionBusy = true
                                            localError = null
                                            localSuccess = null
                                            coroutineScope.launch {
                                                onSendOtp(regEmail, "account verification")
                                                    .onSuccess { msg ->
                                                        localSuccess = msg
                                                        regOtpStep = true
                                                        regResendTimer = 60
                                                        isActionBusy = false
                                                    }
                                                    .onFailure { error ->
                                                        localError = error.message ?: "Failed to send verification code"
                                                        isActionBusy = false
                                                    }
                                            }
                                        }
                                    },
                                    enabled = canSendCode,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                                ) {
                                    if (isActionBusy) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text("Send Verification Code", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        } else {
                            // OTP VERIFICATION STEP
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Tile),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text("📧 Enter 6-Digit Code", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            "We sent a verification code to $regEmail. Enter it below to complete your registration.",
                                            color = SecondaryText,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }

                                Column {
                                    Text("Verification Code (OTP)", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = regOtpCode,
                                        onValueChange = { input ->
                                            regOtpCode = input.filter { it.isDigit() }.take(6)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        placeholder = { Text("6-digit code", color = SecondaryText.copy(alpha = 0.5f)) },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                            },
                                        ),
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
                                        if (regOtpCode.length == 6) {
                                            keyboardController?.hide()
                                            isActionBusy = true
                                            localError = null
                                            localSuccess = null
                                            coroutineScope.launch {
                                                onVerifyOtp(regEmail, regOtpCode)
                                                    .onSuccess {
                                                        isActionBusy = false
                                                        onRegister(regUsername, regEmail, regAddress, regPassword)
                                                    }
                                                    .onFailure { error ->
                                                        localError = error.message ?: "Invalid verification code"
                                                        isActionBusy = false
                                                    }
                                            }
                                        }
                                    },
                                    enabled = regOtpCode.length == 6 && !isActionBusy && !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                                ) {
                                    if (isActionBusy || isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text("Verify & Complete Registration", style = MaterialTheme.typography.titleMedium)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TextButton(
                                        onClick = {
                                            regOtpStep = false
                                            localError = null
                                        },
                                    ) {
                                        Text("← Change Details", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                                    }

                                    TextButton(
                                        onClick = {
                                            if (regResendTimer == 0) {
                                                isActionBusy = true
                                                localError = null
                                                coroutineScope.launch {
                                                    onSendOtp(regEmail, "account verification")
                                                        .onSuccess { msg ->
                                                            localSuccess = msg
                                                            regResendTimer = 60
                                                            isActionBusy = false
                                                        }
                                                        .onFailure { error ->
                                                            localError = error.message ?: "Failed to resend code"
                                                            isActionBusy = false
                                                        }
                                                }
                                            }
                                        },
                                        enabled = regResendTimer == 0 && !isActionBusy,
                                    ) {
                                        Text(
                                            if (regResendTimer > 0) "Resend in ${regResendTimer}s" else "Resend Code",
                                            color = if (regResendTimer == 0) Cyan else SecondaryText.copy(alpha = 0.5f),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
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
                    // ==========================================
                    // FORGOT PASSWORD FLOW
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                isForgotPassword = false
                                forgotStep = 0
                                localError = null
                                localSuccess = null
                                focusManager.clearFocus()
                            },
                        ) {
                            Text("← Back to Sign In", color = Cyan, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Text(
                        text = "Reset Your Password",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )

                    // Feedback Banners
                    if (!localSuccess.isNullOrBlank()) {
                        Surface(
                            color = Green.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Green.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "✓ $localSuccess",
                                color = Green,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                    val displayError = localError ?: errorMessage
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            color = Red.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = displayError,
                                color = Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }

                    if (forgotStep == 0) {
                        // STEP 1: Enter Email for Password Reset
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Enter the email associated with your account. We'll send a 6-digit OTP code to reset your password.",
                                color = SecondaryText,
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Column {
                                Text("Registered Email Address", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = forgotEmail,
                                    onValueChange = { forgotEmail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { keyboardController?.hide() },
                                    ),
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
                                    if (forgotEmail.contains("@") && forgotEmail.contains(".")) {
                                        keyboardController?.hide()
                                        isActionBusy = true
                                        localError = null
                                        localSuccess = null
                                        coroutineScope.launch {
                                            onSendOtp(forgotEmail, "password reset")
                                                .onSuccess { msg ->
                                                    localSuccess = msg
                                                    forgotStep = 1
                                                    forgotResendTimer = 60
                                                    isActionBusy = false
                                                }
                                                .onFailure { error ->
                                                    localError = error.message ?: "Failed to send reset code"
                                                    isActionBusy = false
                                                }
                                        }
                                    }
                                },
                                enabled = forgotEmail.contains("@") && forgotEmail.contains(".") && !isActionBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                            ) {
                                if (isActionBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Send Reset Code", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    } else {
                        // STEP 2: Enter OTP & New Password
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Tile),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text("Code sent to $forgotEmail", color = Green, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("Enter the 6-digit code and your new password below.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Column {
                                Text("6-Digit Code", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = forgotOtpCode,
                                    onValueChange = { input -> forgotOtpCode = input.filter { it.isDigit() }.take(6) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    ),
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
                                Text("New Password (min 6 characters)", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = forgotNewPassword,
                                    onValueChange = { forgotNewPassword = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    ),
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
                                Text("Confirm New Password", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = forgotConfirmPassword,
                                    onValueChange = { forgotConfirmPassword = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done,
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { keyboardController?.hide() },
                                    ),
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

                            val canReset = forgotOtpCode.length == 6 &&
                                forgotNewPassword.length >= 6 &&
                                forgotNewPassword == forgotConfirmPassword &&
                                !isActionBusy

                            Button(
                                onClick = {
                                    if (canReset) {
                                        keyboardController?.hide()
                                        isActionBusy = true
                                        localError = null
                                        localSuccess = null
                                        coroutineScope.launch {
                                            onResetPassword(forgotEmail, forgotOtpCode, forgotNewPassword)
                                                .onSuccess { msg ->
                                                    isActionBusy = false
                                                    loginEmail = forgotEmail
                                                    loginPassword = ""
                                                    isForgotPassword = false
                                                    forgotStep = 0
                                                    localSuccess = "$msg. Please sign in with your new password."
                                                }
                                                .onFailure { error ->
                                                    localError = error.message ?: "Failed to reset password"
                                                    isActionBusy = false
                                                }
                                        }
                                    }
                                },
                                enabled = canReset,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                            ) {
                                if (isActionBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Reset Password", style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                TextButton(onClick = { forgotStep = 0 }) {
                                    Text("← Change Email", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                                }

                                TextButton(
                                    onClick = {
                                        if (forgotResendTimer == 0) {
                                            isActionBusy = true
                                            localError = null
                                            coroutineScope.launch {
                                                onSendOtp(forgotEmail, "password reset")
                                                    .onSuccess { msg ->
                                                        localSuccess = msg
                                                        forgotResendTimer = 60
                                                        isActionBusy = false
                                                    }
                                                    .onFailure { error ->
                                                        localError = error.message ?: "Failed to resend code"
                                                        isActionBusy = false
                                                    }
                                            }
                                        }
                                    },
                                    enabled = forgotResendTimer == 0 && !isActionBusy,
                                ) {
                                    Text(
                                        if (forgotResendTimer > 0) "Resend in ${forgotResendTimer}s" else "Resend Code",
                                        color = if (forgotResendTimer == 0) Cyan else SecondaryText.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // AUTHENTICATED USER PROFILE & TRANSACTIONS
                // ==========================================
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
                        val initial = (userProfile.username.firstOrNull() ?: 'U').uppercaseChar().toString()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = userProfile.username,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Green.copy(alpha = 0.2f),
                            ) {
                                Text(
                                    "Verified",
                                    color = Green,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = userProfile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                        )
                    }
                }

                // Tabs: 0 = Account Details, 1 = Transaction History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("👤 Account Details", "📋 History (${transactions.size})").forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Surface(
                            onClick = {
                                selectedTab = index
                                if (index == 1) {
                                    onRefreshTransactions()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Green else Tile,
                            contentColor = if (selected) Color.White else TextPrimary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // TAB 0: ACCOUNT DETAILS & WALLET
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Tile),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Live Server Balance", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                                Text(formattedBalance, color = Green, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Text("🪙", style = MaterialTheme.typography.headlineMedium)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Username", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                                Text("Letters, numbers, underscores", color = SecondaryText.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editUsername,
                                onValueChange = { input ->
                                    editUsername = input.filter { it.isLetterOrDigit() || it == '_' }.take(24)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
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
                            Text("Email Address (Verified)", color = SecondaryText, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = TileBorder,
                                    disabledContainerColor = Tile.copy(alpha = 0.6f),
                                    disabledTextColor = SecondaryText,
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() },
                                ),
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
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                onLogout()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                        ) {
                            Text("Log Out", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onSaveProfile(editUsername, editEmail, editAddress)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.White),
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // TAB 1: TRANSACTION HISTORY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Transaction History",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )

                        TextButton(
                            onClick = onRefreshTransactions,
                            enabled = !isTransactionsLoading,
                        ) {
                            if (isTransactionsLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Green, strokeWidth = 2.dp)
                            } else {
                                Text("↻ Refresh", color = Cyan, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (isTransactionsLoading && transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Green)
                        }
                    } else if (transactions.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Tile),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("📜", style = MaterialTheme.typography.headlineLarge)
                                Text("No Transactions Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    "Place bets and cash out in the game to see your real-time transaction history here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(transactions, key = { it.id }) { tx ->
                                val isWin = tx.type.equals("WIN", ignoreCase = true)
                                val isBet = tx.type.equals("BET", ignoreCase = true)

                                val badgeColor = when {
                                    isWin -> Green
                                    isBet -> Red
                                    else -> Gold
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Tile),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.5.dp, TileBorder),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = badgeColor.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                                            ) {
                                                Text(
                                                    text = when {
                                                        isWin -> "WIN 🏆"
                                                        isBet -> "BET 🎯"
                                                        else -> tx.type
                                                    },
                                                    color = badgeColor,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = when {
                                                        isWin -> "Cashout Payout"
                                                        isBet -> "Game Bet"
                                                        else -> tx.type
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Text(
                                                    text = tx.formattedDateTime,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SecondaryText,
                                                )
                                            }
                                        }

                                        Text(
                                            text = when {
                                                isWin -> "+${tx.formattedAmount}"
                                                isBet -> "-${tx.formattedAmount}"
                                                else -> tx.formattedAmount
                                            },
                                            color = when {
                                                isWin -> Green
                                                isBet -> Red
                                                else -> TextPrimary
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Red),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red),
                    ) {
                        Text("Log Out", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
