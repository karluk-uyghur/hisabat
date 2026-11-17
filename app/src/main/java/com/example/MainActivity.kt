package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.preferences.ThemePreferences
import com.example.data.repository.AccountRepository
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.HesabatTheme
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.RoseUnpaid
import com.example.ui.theme.TurquoiseAccent
import com.example.ui.viewmodel.AccountViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.security.MessageDigest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // باشقا ئۈسكۈنە ۋە ئىز قوغلاش پىروگراممىلىرىنىڭ ئېكراننى تارتىۋېلىشىدىن قوغداش
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        window.decorView.filterTouchesWhenObscured = true

        val externalCaller = callingPackage ?: referrer?.host
        val isExternalAccess = (intent?.action != null && intent.action != Intent.ACTION_MAIN) ||
            (externalCaller != null && !externalCaller.contains(packageName))

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val themePrefs = remember { ThemePreferences(context.applicationContext) }
            val isDarkGlass by themePrefs.isDarkGlassFlow.collectAsStateWithLifecycle(initialValue = true)

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HesabatTheme(darkTheme = isDarkGlass) {
                    AppNavigation(
                        isDarkGlass = isDarkGlass,
                        isExternalAccess = isExternalAccess,
                        externalCallerName = externalCaller ?: "سىرتقى پىروگرامما",
                        onRejectExternal = { finish() },
                        onToggleTheme = {
                            scope.launch { themePrefs.setDarkGlass(!isDarkGlass) }
                        }
                    )
                }
            }
        }
    }
}

data class LocalUserAccount(
    val userId: String,
    val username: String,
    val passwordHash: String
)

class LocalAuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("hesabat_secure_auth_prefs", Context.MODE_PRIVATE)

    fun getRegisteredUsers(): List<LocalUserAccount> {
        val raw = prefs.getString(KEY_USERS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<LocalUserAccount>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    LocalUserAccount(
                        userId = obj.optString("userId"),
                        username = obj.optString("username"),
                        passwordHash = obj.optString("passwordHash")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getActiveUser(): LocalUserAccount? {
        val activeId = prefs.getString(KEY_ACTIVE_USER_ID, null) ?: return null
        return getRegisteredUsers().firstOrNull { it.userId == activeId }
    }

    fun registerOrLoginGoogleUser(googleEmailOrName: String): LocalUserAccount {
        val clean = googleEmailOrName.trim().ifBlank { "Google ھېساباتى" }
        val userId = "user_" + sha256(clean.lowercase()).take(16)
        val users = getRegisteredUsers().toMutableList()
        val existing = users.firstOrNull { it.userId == userId || it.username.equals(clean, ignoreCase = true) }
        val account = existing ?: LocalUserAccount(
            userId = userId,
            username = clean,
            passwordHash = sha256("google_$userId")
        ).also {
            users.add(it)
            saveUsers(users)
        }
        prefs.edit().putString(KEY_ACTIVE_USER_ID, account.userId).commit()
        return account
    }

    fun registerUser(username: String, password: String): Result<LocalUserAccount> {
        val cleanName = username.trim()
        if (cleanName.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("ھېسابات ئىسمى ۋە مەخپىي نومۇرنى تولدۇرۇڭ"))
        }
        val users = getRegisteredUsers().toMutableList()
        val existing = users.firstOrNull { it.username.equals(cleanName, ignoreCase = true) }
        if (existing != null) {
            if (existing.passwordHash == sha256(password)) {
                prefs.edit().putString(KEY_ACTIVE_USER_ID, existing.userId).commit()
                return Result.success(existing)
            }
            return Result.failure(IllegalArgumentException("بۇ ھېسابات بۇرۇن ئېچىلغان، «ھېساباتقا كىرىش» نى بېسىپ شۇ مەخپىي نومۇر بىلەن كىرىڭ"))
        }
        val userId = "user_" + sha256(cleanName.lowercase()).take(16)
        val account = LocalUserAccount(
            userId = userId,
            username = cleanName,
            passwordHash = sha256(password)
        )
        users.add(account)
        saveUsers(users)
        prefs.edit().putString(KEY_ACTIVE_USER_ID, account.userId).commit()
        return Result.success(account)
    }

    fun loginUser(username: String, password: String): Result<LocalUserAccount> {
        val cleanName = username.trim()
        val users = getRegisteredUsers()
        val matched = users.firstOrNull { it.username.equals(cleanName, ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("بۇ ھېسابات تېپىلمىدى، ئالدى بىلەن يېڭى ھېسابات ئېچىڭ"))
        if (matched.passwordHash != sha256(password)) {
            return Result.failure(IllegalArgumentException("مەخپىي نومۇر خاتا!"))
        }
        prefs.edit().putString(KEY_ACTIVE_USER_ID, matched.userId).commit()
        return Result.success(matched)
    }

    fun verifyPassword(user: LocalUserAccount, password: String): Boolean {
        return user.passwordHash == sha256(password)
    }

    fun logout() {
        prefs.edit().remove(KEY_ACTIVE_USER_ID).commit()
        runCatching { Firebase.auth.signOut() }
    }

    private fun saveUsers(users: List<LocalUserAccount>) {
        val arr = JSONArray()
        users.forEach { u ->
            arr.put(
                JSONObject().apply {
                    put("userId", u.userId)
                    put("username", u.username)
                    put("passwordHash", u.passwordHash)
                }
            )
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).commit()
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_USERS = "registered_accounts_v2"
        const val KEY_ACTIVE_USER_ID = "active_user_id_v2"
    }
}

@Composable
fun AppNavigation(
    isDarkGlass: Boolean,
    isExternalAccess: Boolean = false,
    externalCallerName: String = "",
    onRejectExternal: () -> Unit = {},
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val authManager = remember { LocalAuthManager(context.applicationContext) }
    val repository = remember { AccountRepository(context.applicationContext) }

    var registeredUsers by remember { mutableStateOf(authManager.getRegisteredUsers()) }
    var currentUser by remember { mutableStateOf(authManager.getActiveUser()) }
    var needExternalPermission by remember { mutableStateOf(isExternalAccess) }
    var manualLockActive by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    if (needExternalPermission) {
        ExternalProgramPermissionDialog(
            callerName = externalCallerName,
            onAllow = { needExternalPermission = false },
            onDeny = onRejectExternal
        )
    }

    if (showGoogleDialog) {
        GoogleAccountQuickDialog(
            onDismiss = { showGoogleDialog = false },
            onConfirmGoogleAccount = { emailOrName ->
                val user = authManager.registerOrLoginGoogleUser(emailOrName)
                registeredUsers = authManager.getRegisteredUsers()
                currentUser = user
                showGoogleDialog = false
            }
        )
    }

    val activeAccount = currentUser
    if (activeAccount == null) {
        AccountPortalScreen(
            registeredUsers = registeredUsers,
            onLogin = { username: String, password: String ->
                val res = authManager.loginUser(username, password)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                }
                res.map { Unit }
            },
            onRegister = { username: String, password: String ->
                val res = authManager.registerUser(username, password)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                }
                res.map { Unit }
            },
            onGoogleSignInClick = {
                attemptAutoSignIn(
                    context = context,
                    credentialManager = credentialManager,
                    onAuthSuccess = {
                        val fbUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                        val googleName = fbUser?.email ?: fbUser?.displayName ?: "Google ھېساباتى"
                        val user = authManager.registerOrLoginGoogleUser(googleName)
                        registeredUsers = authManager.getRegisteredUsers()
                        currentUser = user
                    },
                    onUnauthenticated = {
                        showGoogleDialog = true
                    },
                    scope = scope
                )
            }
        )
    } else {
        if (manualLockActive) {
            PasswordPermissionDialog(
                username = activeAccount.username,
                onVerify = { pwd: String ->
                    if (authManager.verifyPassword(activeAccount, pwd)) {
                        manualLockActive = false
                        true
                    } else {
                        false
                    }
                },
                onSwitchAccount = {
                    manualLockActive = false
                    authManager.logout()
                    currentUser = null
                }
            )
        }

        val uid = activeAccount.userId
        key(uid) {
            val accountViewModel: AccountViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        AccountViewModel(repository, uid)
                    }
                }
            )
            DashboardScreen(
                viewModel = accountViewModel,
                isDarkGlass = isDarkGlass,
                onToggleTheme = onToggleTheme,
                currentUsername = activeAccount.username,
                onLockApp = { manualLockActive = true },
                onSignOut = {
                    authManager.logout()
                    currentUser = null
                }
            )
        }
    }
}

@Composable
private fun GoogleAccountQuickDialog(
    onDismiss: () -> Unit,
    onConfirmGoogleAccount: (String) -> Unit
) {
    var googleAccountInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = TurquoiseAccent,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text("Google ھېساباتى بىلەن كىرىش", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Google ئېلخەت ئادرېسىڭىزنى (Gmail) ياكى ھېسابات نامىڭىزنى كىرگۈزۈڭ. بۇ ھېساباتنىڭ بارلىق مەلۇماتلىرى مەڭگۈلۈك ساقلىنىدۇ:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = googleAccountInput,
                    onValueChange = { googleAccountInput = it },
                    label = { Text("Gmail ياكى Google ھېسابات نامى") },
                    placeholder = { Text("example@gmail.com") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = googleAccountInput.trim().ifEmpty { "Google ھېساباتى" }
                    onConfirmGoogleAccount(finalName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) {
                Text("كىرىش", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بىكار قىلىش")
            }
        }
    )
}

@Composable
private fun ExternalProgramPermissionDialog(
    callerName: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = RoseUnpaid,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "سىرتقى پىروگرامما كىرىش ئىجازىتى",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "«$callerName» بۇ ئەپكە ئۇلانماقچى بولدى. ئىز قوغلاشنى چەكلەش سىستېمىسى ئۇنى توسۇپ قالدى. ئىجازەت بېرەمسىز؟",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("ئىجازەت مەخپىي نومۇرى (ئىختىيارىي)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid)
            ) {
                Text("ئىجازەت بېرىش", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDeny,
                colors = ButtonDefaults.buttonColors(containerColor = RoseUnpaid)
            ) {
                Text("رەت قىلىش", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PasswordPermissionDialog(
    username: String,
    onVerify: (String) -> Boolean,
    onSwitchAccount: () -> Unit
) {
    var pwd by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(38.dp))
        },
        title = {
            Text("بىخەتەرلىك ئىجازەت قۇلۇپى", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("«$username» ھېساباتىنى ئېچىش ئۈچۈن مەخپىي نومۇرنى كىرگۈزۈڭ:")
                OutlinedTextField(
                    value = pwd,
                    onValueChange = {
                        pwd = it
                        err = null
                    },
                    label = { Text("مەخپىي نومۇر") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                if (err != null) {
                    Text(err.orEmpty(), color = RoseUnpaid, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!onVerify(pwd)) {
                        err = "مەخپىي نومۇر خاتا!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) {
                Text("ئېچىش", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onSwitchAccount) {
                Text("باشقا ھېساباتقا ئالمىشىش")
            }
        }
    )
}

@Composable
private fun AccountPortalScreen(
    registeredUsers: List<LocalUserAccount>,
    onLogin: (String, String) -> Result<Unit>,
    onRegister: (String, String) -> Result<Unit>,
    onGoogleSignInClick: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(registeredUsers.isEmpty()) }
    var username by remember { mutableStateOf(registeredUsers.firstOrNull()?.username.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_landscape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xAA060B19), Color(0xEE081024)))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.22f)),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Text(
                        text = "ھېسابات باشقۇرۇش سىستېمىسى",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldPaid.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, EmeraldPaid.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = EmeraldPaid, modifier = Modifier.size(16.dp))
                            Text(
                                text = "مەڭگۈلۈك ساقلاش ئامبىرى • ئىز قوغلاشتىن قوغدالغان",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isRegisterMode,
                            onClick = {
                                isRegisterMode = false
                                errorMessage = null
                            },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text("ھېساباتقا كىرىش") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                errorMessage = null
                            },
                            leadingIcon = { Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text("يېڭى ھېسابات ئېچىش") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = registeredUsers.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ساقلانغان ھېساباتلار (چېكىپ تاللاڭ):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                registeredUsers.forEach { acc ->
                                    FilterChip(
                                        selected = username.equals(acc.username, ignoreCase = true),
                                        onClick = {
                                            username = acc.username
                                            isRegisterMode = false
                                        },
                                        leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPaid) },
                                        label = { Text(acc.username) }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("ھېسابات ئىسمى *") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IndigoAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Badge, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(18.dp))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("مەخپىي نومۇر *") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TurquoiseAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(18.dp))
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage.orEmpty(),
                            color = RoseUnpaid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            val res = if (isRegisterMode) {
                                onRegister(username, password)
                            } else {
                                onLogin(username, password)
                            }
                            res.onFailure { err -> errorMessage = err.message }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isRegisterMode) Icons.Filled.PersonAdd else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) "ھېسابات ئېچىپ كىرىش" else "ھېساباتقا كىرىش",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, TurquoiseAccent.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = TurquoiseAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google ھېساباتى بىلەن كىرىش",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
