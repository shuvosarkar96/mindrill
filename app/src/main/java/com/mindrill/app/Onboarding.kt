package com.mindrill.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import com.mindrill.app.ui.theme.MindrillTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

enum class Stage { Welcome, Consent, Identity, Otp, Main }

@Composable
fun MindrillApp() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val systemDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(prefs.darkOrNull() ?: systemDark) }
    var stage by rememberSaveable {
        mutableStateOf(if (prefs.loggedIn) Stage.Main else Stage.Welcome)
    }
    var userName by rememberSaveable { mutableStateOf(prefs.userName) }

    MindrillTheme(darkTheme = dark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (stage) {
                Stage.Welcome -> WelcomeScreen(
                    onStart = { stage = Stage.Consent },
                    onLogin = { stage = Stage.Identity }
                )
                Stage.Consent -> ConsentScreen(
                    onBack = { stage = Stage.Welcome },
                    onConfirm = { stage = Stage.Identity }
                )
                Stage.Identity -> IdentityScreen(
                    onBack = { stage = Stage.Consent },
                    onContinue = { name -> userName = name; stage = Stage.Otp }
                )
                Stage.Otp -> OtpScreen(
                    onBack = { stage = Stage.Identity },
                    onVerify = {
                        prefs.loggedIn = true
                        prefs.userName = userName
                        stage = Stage.Main
                    }
                )
                Stage.Main -> MainScaffold(
                    userName = userName,
                    onLogout = {
                        prefs.loggedIn = false
                        stage = Stage.Welcome
                    },
                    darkMode = dark,
                    onDarkMode = { dark = it; prefs.setDark(it) }
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun StepScaffold(
    step: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Step $step of 2",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LinearProgressIndicator(
            progress = { step / 2f },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        content()
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit, onLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Construction,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "mindrill",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        Column(
            modifier = Modifier.navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to mindrill", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Your all-in-one mining marketplace, map and chat.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            PrimaryButton("Get Started", onClick = onStart)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onLogin) { Text("Have an account?  Log in") }
        }
    }
}

@Composable
fun ConsentScreen(onBack: () -> Unit, onConfirm: () -> Unit) {
    var adult by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Enable location", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("See mines and markets near you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(32.dp))
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Before you continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "mindrill collects basic information to keep the community safe. You must be 18 or older to use this app.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = adult, onCheckedChange = { adult = it })
                    Text("I am 18 or older")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Confirm", enabled = adult, onClick = onConfirm)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun IdentityScreen(onBack: () -> Unit, onContinue: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    StepScaffold(step = 1, onBack = onBack) {
        Text("Verify your identity", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("This keeps mindrill safe for everyone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            placeholder = { Text("+880 1XXX XXXXXX") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            "Continue",
            enabled = name.isNotBlank() && phone.filter { it.isDigit() }.length >= 10,
            onClick = { onContinue(name.trim()) }
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun OtpScreen(onBack: () -> Unit, onVerify: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    StepScaffold(step = 2, onBack = onBack) {
        Text("Enter the code", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("We sent a 6-digit code to your phone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        BasicTextField(
            value = code,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { i ->
                        val active = i == code.length
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                code.getOrNull(i)?.toString() ?: "",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Resend code in 2:59",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Demo mode: any 6 digits will work for now.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton("Verify and continue", enabled = code.length == 6, onClick = onVerify)
        Spacer(Modifier.height(16.dp))
    }
}