package com.example.wgmanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.AppLanguage
import com.example.wgmanager.data.AppStrings
import com.example.wgmanager.data.DataStore
import com.example.wgmanager.data.UserRole
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import kotlinx.coroutines.launch
import kotlin.math.sin

private val LoginPurple = Color(0xFF7C3AED)
private val LoginPurpleLight = Color(0xFFA78BFA)

private data class LoginFloatingEmoji(val emoji: String, val x: Dp, val y: Dp, val alpha: Float, val phase: Float)

@Composable
fun LoginScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    var screenMode by remember { mutableStateOf("login") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var twoFACode by remember { mutableStateOf("") }
    var pendingUser by remember { mutableStateOf<com.example.wgmanager.data.User?>(null) }
    var showCookieBanner by remember { mutableStateOf(true) }
    var demoIndex by remember { mutableIntStateOf(0) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var langRefresh by remember { mutableIntStateOf(0) }
    val demoAccounts = listOf("user@wg.com", "admin@wg.com", "super@wg.com", "new@wg.com", "2fa@wg.com")
    
    val s = AppStrings

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "floatAnim"
    )

    val cardAlpha = remember { Animatable(0f) }
    val cardOffset = remember { Animatable(40f) }
    LaunchedEffect(Unit) {
        launch { cardAlpha.animateTo(1f, tween(500)) }
        cardOffset.animateTo(0f, spring(dampingRatio = 0.7f))
    }

    val emojis = remember {
        listOf(
            LoginFloatingEmoji("📡", 20.dp, 60.dp, 0.22f, 0f),
            LoginFloatingEmoji("🏠", 260.dp, 50.dp, 0.22f, 0.5f),
            LoginFloatingEmoji("📱", 50.dp, 130.dp, 0.18f, 1f),
            LoginFloatingEmoji("🏢", 310.dp, 160.dp, 0.16f, 1.5f),
            LoginFloatingEmoji("📶", 30.dp, 550.dp, 0.18f, 2f),
            LoginFloatingEmoji("🔑", 280.dp, 600.dp, 0.16f, 2.5f),
            LoginFloatingEmoji("🛡️", 320.dp, 380.dp, 0.14f, 3f),
            LoginFloatingEmoji("👥", 40.dp, 320.dp, 0.14f, 3.5f),
            LoginFloatingEmoji("🧹", 150.dp, 90.dp, 0.12f, 4f),
            LoginFloatingEmoji("🛒", 200.dp, 520.dp, 0.14f, 4.5f),
            LoginFloatingEmoji("📅", 100.dp, 450.dp, 0.12f, 5f),
            LoginFloatingEmoji("🍕", 330.dp, 280.dp, 0.10f, 5.5f),
            LoginFloatingEmoji("💰", 80.dp, 680.dp, 0.12f, 6f),
            LoginFloatingEmoji("🎉", 240.dp, 700.dp, 0.14f, 6.5f),
        )
    }

    fun routeUser(user: com.example.wgmanager.data.User) {
        AppStrings.setLanguage(if (user.language == "EN") AppLanguage.EN else AppLanguage.DE)
        when {
            user.isBanned -> { errorMsg = "\uD83D\uDEAB Account banned"; DataStore.logout(); return }
            user.role == UserRole.SUPER_ADMIN -> onNavigate(AppScreen.SYSTEM_PANEL)
            !user.hasWG -> onNavigate(AppScreen.WG_FINDER)
            user.hasWG && !user.onboardingCompleted -> onNavigate(AppScreen.ONBOARDING)
            else -> onNavigate(AppScreen.DASHBOARD)
        }
    }

    fun doLogin() {
        errorMsg = null
        val user = DataStore.login(email.trim(), password)
        if (user == null) { errorMsg = s.invalidCredentials; return }
        if (user.isTwoFactorEnabled) { screenMode = "2fa"; pendingUser = user }
        else { toast.show("${s.welcomeBack}, ${user.name}!"); routeUser(user) }
    }

    fun doRegister() {
        errorMsg = null
        if (name.isBlank() || email.isBlank() || password.isBlank()) { errorMsg = s.allFieldsRequired; return }
        if (!agreeTerms) { errorMsg = s.acceptTerms; return }
        val user = DataStore.register(name.trim(), email.trim(), password)
        if (user == null) { errorMsg = s.emailExists; return }
        toast.show("${s.accountCreated} ${user.name}! 🎉")
        routeUser(user)
    }

    fun verify2FA() {
        if (twoFACode == "123456") {
            val u = pendingUser ?: return
            DataStore.currentUser = u
            toast.show("${s.codeVerified} ✅")
            routeUser(u)
        } else { errorMsg = s.invalidCode }
    }
    
    fun toggleLanguage() {
        AppStrings.toggleLanguage()
        langRefresh++
    }

    val currentLang = remember(langRefresh) { AppStrings.currentLanguage }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        emojis.forEach { emoji ->
            val yOffset = sin((floatProgress * 2 * Math.PI + emoji.phase).toFloat()) * 12f
            val xOffset = sin((floatProgress * 2 * Math.PI + emoji.phase + 1).toFloat()) * 6f
            val rotation = sin((floatProgress * 2 * Math.PI + emoji.phase).toFloat()) * 8f
            val alphaAnim = 0.7f + sin((floatProgress * 2 * Math.PI + emoji.phase).toFloat()) * 0.3f
            
            Text(
                emoji.emoji, fontSize = 28.sp,
                modifier = Modifier
                    .offset(x = emoji.x + xOffset.dp, y = emoji.y + yOffset.dp)
                    .graphicsLayer(rotationZ = rotation, alpha = emoji.alpha * alphaAnim)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    shape = RoundedCornerShape(20.dp), color = Color(0xFF374151),
                    modifier = Modifier.clickable { toggleLanguage() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(if (currentLang == AppLanguage.DE) "DE" else "EN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(50.dp))

            Box(contentAlignment = Alignment.Center) {
                val logoFloat = sin((floatProgress * 2 * Math.PI).toFloat()) * 12f
                val logoScale = 1f + sin((floatProgress * 2 * Math.PI).toFloat()) * 0.04f
                val logoRotation = sin((floatProgress * 2 * Math.PI + 0.5f).toFloat()) * 3f
                Surface(
                    shape = RoundedCornerShape(24.dp), color = Color.Transparent,
                    modifier = Modifier.size(90.dp)
                        .offset(y = logoFloat.dp)
                        .graphicsLayer(scaleX = logoScale, scaleY = logoScale, rotationZ = logoRotation)
                        .background(Brush.linearGradient(listOf(LoginPurple, LoginPurpleLight)), RoundedCornerShape(24.dp))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Home, "Home", tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            when (screenMode) {
                "login", "2fa" -> {
                    Text(s.greeting, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text(s.loginSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                "register" -> {
                    Text(s.joinTheClub, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text(s.registerSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth().offset(y = cardOffset.value.dp).graphicsLayer(alpha = cardAlpha.value)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when (screenMode) {
                        "login" -> LoginForm(email, { email = it }, password, { password = it }, showPassword, { showPassword = it },
                            rememberMe, { rememberMe = it }, errorMsg, s, toast, { doLogin() }, { screenMode = "register"; errorMsg = null },
                            demoAccounts, demoIndex, { email = demoAccounts[demoIndex]; password = "1234"; demoIndex = (demoIndex + 1) % demoAccounts.size },
                            { showForgotDialog = true })
                        "register" -> RegisterForm(name, { name = it }, email, { email = it }, password, { password = it },
                            showPassword, { showPassword = it }, agreeTerms, { agreeTerms = it }, errorMsg, s,
                            { doRegister() }, { screenMode = "login"; errorMsg = null },
                            onShowTerms = { showTermsDialog = true }, onShowPrivacy = { showPrivacyDialog = true })
                        "2fa" -> TwoFAForm(twoFACode, { twoFACode = it }, errorMsg, pendingUser, s, floatProgress,
                            { verify2FA() }, { screenMode = "login"; errorMsg = null; twoFACode = "" }, toast)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("○  ", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                Text(s.privacyLabel, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { showPrivacyDialog = true })
                Text("  •  ", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                Text(s.termsOfService, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { showTermsDialog = true })
                Text("  •  v1.0.2", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(80.dp))
        }

        AnimatedVisibility(visible = showCookieBanner, modifier = Modifier.align(Alignment.BottomCenter)) {
            Surface(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🍪", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(s.cookieBanner, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { showCookieBanner = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(s.accept, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showTermsDialog) {
        LoginInfoDialog(s.termsTitle, { showTermsDialog = false }) {
            Column {
                Text("📋", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(8.dp))
                Text(s.termsContent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
    if (showPrivacyDialog) {
        LoginInfoDialog(s.privacyTitle, { showPrivacyDialog = false }) {
            Column {
                Text("🔒", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(8.dp))
                Text(s.privacyContent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
    
    if (showForgotDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotDialog = false },
            s = s,
            toast = toast
        )
    }
}

@Composable
private fun LoginForm(email: String, onEmail: (String) -> Unit, password: String, onPassword: (String) -> Unit,
    showPassword: Boolean, onShowPassword: (Boolean) -> Unit, rememberMe: Boolean, onRememberMe: (Boolean) -> Unit,
    errorMsg: String?, s: AppStrings, toast: ToastState, onLogin: () -> Unit, onRegister: () -> Unit,
    demoAccounts: List<String>, demoIndex: Int, onDemo: () -> Unit, onForgot: () -> Unit) {
    OutlinedTextField(value = email, onValueChange = onEmail, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
        placeholder = { Text(s.email, color = MaterialTheme.colorScheme.outline) }, leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.outline) },
        colors = loginTfColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = password, onValueChange = onPassword, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
        placeholder = { Text(s.password, color = MaterialTheme.colorScheme.outline) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline) },
        trailingIcon = { IconButton(onClick = { onShowPassword(!showPassword) }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.outline) } },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), colors = loginTfColors())
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = rememberMe, onCheckedChange = onRememberMe, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C3AED), uncheckedColor = MaterialTheme.colorScheme.outline))
        Text(s.rememberMe, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        Text(s.forgotPassword, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { onForgot() })
    }
    errorMsg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall) }
    Spacer(Modifier.height(16.dp))
    GradientBtn(s.login, listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)), onLogin)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Text(s.noAccount, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(4.dp))
        Text(s.registerLink, color = Color(0xFF7C3AED), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { onRegister() })
    }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onClick = onDemo, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
        Text("✨", fontSize = 16.sp); Spacer(Modifier.width(8.dp)); Text(s.demoMode, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp)); Text("(${demoIndex + 1}/${demoAccounts.size})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun RegisterForm(name: String, onName: (String) -> Unit, email: String, onEmail: (String) -> Unit,
    password: String, onPassword: (String) -> Unit, showPassword: Boolean, onShowPassword: (Boolean) -> Unit,
    agreeTerms: Boolean, onAgree: (Boolean) -> Unit, errorMsg: String?, s: AppStrings,
    onRegister: () -> Unit, onLogin: () -> Unit,
    onShowTerms: () -> Unit = {}, onShowPrivacy: () -> Unit = {}) {
    OutlinedTextField(value = name, onValueChange = onName, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
        placeholder = { Text(s.name, color = MaterialTheme.colorScheme.outline) }, leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.outline) }, colors = loginTfColors())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = email, onValueChange = onEmail, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
        placeholder = { Text(s.email, color = MaterialTheme.colorScheme.outline) }, leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.outline) },
        colors = loginTfColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = password, onValueChange = onPassword, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
        placeholder = { Text(s.password, color = MaterialTheme.colorScheme.outline) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline) },
        trailingIcon = { IconButton(onClick = { onShowPassword(!showPassword) }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.outline) } },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), colors = loginTfColors())
    
    // Password Strength - Enhanced algorithm
    if (password.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        val hasLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val criteriaCount = listOf(hasLength, hasUppercase, hasNumber, hasSpecial).count { it }
        
        val strength = when {
            criteriaCount >= 3 && password.length >= 8 -> 2 // Strong
            criteriaCount >= 2 || password.length >= 6 -> 1 // Good
            else -> 0 // Weak
        }
        val strengthColor = when(strength) {
            0 -> Color(0xFFEF4444) // Red
            1 -> Color(0xFFF59E0B) // Orange
            else -> Color(0xFF22C55E) // Green
        }
        val strengthText = when(strength) {
            0 -> s.weak
            1 -> s.good
            else -> s.strong
        }
        val strengthEmoji = when(strength) {
            0 -> "🔴"
            1 -> "🟠"
            else -> "🟢"
        }
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().height(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                    .background(if (strength >= 0) strengthColor else MaterialTheme.colorScheme.surfaceVariant))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                    .background(if (strength >= 1) strengthColor else MaterialTheme.colorScheme.surfaceVariant))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                    .background(if (strength >= 2) strengthColor else MaterialTheme.colorScheme.surfaceVariant))
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(strengthEmoji, fontSize = 10.sp)
                Spacer(Modifier.width(4.dp))
                Text(strengthText, color = strengthColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
            
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = agreeTerms, onCheckedChange = onAgree, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7C3AED), uncheckedColor = MaterialTheme.colorScheme.outline))
        Text(s.agreeToTerms, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(
            s.termsOfService,
            color = Color(0xFF7C3AED),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onShowTerms() }
        )
        Text(" ${s.and} ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(
            s.privacyPolicy,
            color = Color(0xFF7C3AED),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onShowPrivacy() }
        )
    }
    errorMsg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall) }
    Spacer(Modifier.height(16.dp))
    GradientBtn(s.register, listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)), onRegister)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        Text(s.haveAccount, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(4.dp))
        Text(s.login, color = Color(0xFF7C3AED), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { onLogin() })
    }
}
 
@Composable
private fun ForgotPasswordDialog(onDismiss: () -> Unit, s: AppStrings, toast: ToastState) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF374151),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(if (sent) "✉️" else "🔑", fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                
                Text(s.resetPassword, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(s.resetEmailSent, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it; error = null; sent = false }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(14.dp), 
                    singleLine = true,
                    placeholder = { Text(s.email, color = MaterialTheme.colorScheme.outline) }, 
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.outline) },
                    colors = loginTfColors(), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(Modifier.height(24.dp))
                GradientBtn(s.sendLink, listOf(Color(0xFF3B82F6), Color(0xFFA78BFA))) {
                    if (email.contains("@") && email.contains(".")) {
                        sent = true
                        error = null
                    } else {
                        error = s.enterValidEmail
                        sent = false
                    }
                }

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(s.backToLogin, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Error banner
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(s.resetErrorTitle, color = Color(0xFFFCA5A5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(s.resetErrorMsg, color = Color(0xFFFCA5A5), fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                // Success banner
                if (sent) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(s.resetSuccessTitle, color = Color(0xFFD1FAE5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${s.linkSentTo} $email", color = Color(0xFFD1FAE5), fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(s.resetSuccessMsg, color = Color(0xFFA7F3D0), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoFAForm(code: String, onCode: (String) -> Unit, errorMsg: String?, user: com.example.wgmanager.data.User?,
    s: AppStrings, floatProgress: Float, onVerify: () -> Unit, onCancel: () -> Unit, toast: ToastState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val shieldFloat = sin((floatProgress * 2 * Math.PI).toFloat()) * 6f
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(72.dp).offset(y = shieldFloat.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Icon(Icons.Default.Security, "2FA", tint = Color(0xFF7C3AED), modifier = Modifier.size(36.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        Text(s.twoFactorAuth, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(s.enterCode, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        user?.let { Text("${s.codeSentTo}: ${it.email}", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall) }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = code, onValueChange = { onCode(it.take(6)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
            placeholder = { Text("○ ○ ○ ○ ○ ○", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.outline) }, colors = loginTfColors(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        errorMsg?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(20.dp))
        GradientBtn(s.verify, listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)), onVerify)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { toast.show("Code erneut gesendet! 📧") }) { Text(s.resendCode, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        TextButton(onClick = onCancel) { Text(s.cancel, color = MaterialTheme.colorScheme.outline) }
    }
}

@Composable private fun loginTfColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color(0xFF475569), focusedBorderColor = Color(0xFF7C3AED),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = Color(0xFF7C3AED))

@Composable private fun GradientBtn(text: String, colors: List<Color>, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable private fun LoginInfoDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF374151))) {
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    content()
                }
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), border = ButtonDefaults.outlinedButtonBorder(enabled = true)) {
                    Text(AppStrings.close, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
