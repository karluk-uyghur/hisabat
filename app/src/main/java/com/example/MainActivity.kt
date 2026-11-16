package com.example

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.preferences.ThemePreferences
import com.example.data.repository.AccountRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.HesabatTheme
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.RoseUnpaid
import com.example.ui.theme.TurquoiseAccent
import com.example.ui.viewmodel.AccountViewModel
import java.security.MessageDigest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // باشقا ئۈسكۈنە ۋە ئىز قوغلاش پىروگراممىلىرىنىڭ ئېكراننى تارتىشى ياكى كۆزىتىشىنى چەكلەش
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        window.decorView.filterTouchesWhenObscured = true

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
    val passwordHash: String,
    val pinHash: String,
    val requireAuthOnResume: Boolean = true
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
                        passwordHash = obj.optString("passwordHash"),
                        pinHash = obj.optString("pinHash"),
                        requireAuthOnResume = obj.optBoolean("requireAuthOnResume", true)
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

    fun registerUser(
        username: String,
        password: String,
        pinCode: String,
        requireAuthOnResume: Boolean
    ): Result<LocalUserAccount> {
        val cleanName = username.trim()
        if (cleanName.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("ھېسابات ئىسمى ۋە مەخپىي نومۇرنى تولدۇرۇڭ"))
        }
        val users = getRegisteredUsers().toMutableList()
        if (users.any { it.username.equals(cleanName, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("بۇ ھېسابات ئىسمى بۇرۇن تىزىملىتىلغان، كىرىش بېتىدىن كىرىڭ"))
        }
        val userId = "user_" + sha256(cleanName.lowercase()).take(16)
        val effectivePin = pinCode.trim().ifEmpty { password }
        val account = LocalUserAccount(
            userId = userId,
            username = cleanName,
            passwordHash = sha256(password),
            pinHash = sha256(effectivePin),
            requireAuthOnResume = requireAuthOnResume
        )
        users.add(account)
        saveUsers(users)
        prefs.edit().putString(KEY_ACTIVE_USER_ID, account.userId).apply()
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
        prefs.edit().putString(KEY_ACTIVE_USER_ID, matched.userId).apply()
        return Result.success(matched)
    }

    fun verifyUnlockCode(user: LocalUserAccount, code: String): Boolean {
        val hashed = sha256(code)
        return hashed == user.pinHash || hashed == user.passwordHash
    }

    fun logout() {
        prefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
    }

    private fun saveUsers(users: List<LocalUserAccount>) {
        val arr = JSONArray()
        users.forEach { u ->
            arr.put(
                JSONObject().apply {
                    put("userId", u.userId)
                    put("username", u.username)
                    put("passwordHash", u.passwordHash)
                    put("pinHash", u.pinHash)
                    put("requireAuthOnResume", u.requireAuthOnResume)
                }
            )
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply()
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
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { LocalAuthManager(context.applicationContext) }
    val repository = remember { AccountRepository(context.applicationContext) }

    var registeredUsers by remember { mutableStateOf(authManager.getRegisteredUsers()) }
    var currentUser by remember { mutableStateOf(authManager.getActiveUser()) }
    var isSessionLocked by remember {
        mutableStateOf(currentUser?.requireAuthOnResume == true)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentUser) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && currentUser?.requireAuthOnResume == true) {
                isSessionLocked = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activeAccount = currentUser
    if (activeAccount == null) {
        LocalAccountAuthScreen(
            registeredUsers = registeredUsers,
            onLogin = { username, password ->
                val res = authManager.loginUser(username, password)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                    isSessionLocked = false
                }
                res.map { Unit }
            },
            onRegister = { username, password, pin, lockOnResume ->
                val res = authManager.registerUser(username, password, pin, lockOnResume)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                    isSessionLocked = false
                }
                res.map { Unit }
            }
        )
    } else if (isSessionLocked) {
        SecurityGateScreen(
            user = activeAccount,
            onVerifyPin = { code ->
                if (authManager.verifyUnlockCode(activeAccount, code)) {
                    isSessionLocked = false
                    true
                } else {
                    false
                }
            },
            onSwitchAccount = {
                authManager.logout()
                currentUser = null
                isSessionLocked = false
            }
        )
    } else {
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
                onLockApp = { isSessionLocked = true },
                onSignOut = {
                    authManager.logout()
                    currentUser = null
                    isSessionLocked = false
                }
            )
        }
    }
}

@Composable
private fun LocalAccountAuthScreen(
    registeredUsers: List<LocalUserAccount>,
    onLogin: (String, String) -> Result<Unit>,
    onRegister: (String, String, String, Boolean) -> Result<Unit>
) {
    var isRegisterMode by remember { mutableStateOf(registeredUsers.isEmpty()) }
    var username by remember { mutableStateOf(registeredUsers.firstOrNull()?.username.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var requirePinOnOpen by remember { mutableStateOf(true) }
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
                    Brush.verticalGradient(
                        listOf(Color(0xCC060B19), Color(0xF2081024))
                    )
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
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(30.dp),
                color = Color(0xD9111827),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.18f)),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "ھېسابات مەخپىي ئامبىرى",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldPaid.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, EmeraldPaid.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = EmeraldPaid,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "ئىز قوغلاشتىن قوغدالغان • مۇستەقىل يەرلىك ساندان",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
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
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            label = { Text("ھېساباتقا كىرىش") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                errorMessage = null
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            label = { Text("يېڭى ھېسابات ئېچىش") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isRegisterMode && registeredUsers.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ساقلانغان ھېساباتلار (چېكىپ تاللاڭ):",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
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
                                        onClick = { username = acc.username },
                                        leadingIcon = {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
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
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = TurquoiseAccent) },
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
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = IndigoAccent) },
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

                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { pinCode = it },
                                label = { Text("ئىجازەت كودى / PIN (ئىختىيارىي)") },
                                leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null, tint = EmeraldPaid) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ئەپ قايتا ئېچىلغاندا ئىجازەت كودى سورىسۇن",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Switch(
                                    checked = requirePinOnOpen,
                                    onCheckedChange = { requirePinOnOpen = it }
                                )
                            }
                        }
                    }

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
                            val result = if (isRegisterMode) {
                                onRegister(username, password, pinCode, requirePinOnOpen)
                            } else {
                                onLogin(username, password)
                            }
                            result.onFailure { err ->
                                errorMessage = err.message
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isRegisterMode) Icons.Filled.PersonAdd else Icons.Filled.Lock,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) "ھېسابات ئېچىپ كىرىش" else "ھېساباتقا كىرىش",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityGateScreen(
    user: LocalUserAccount,
    onVerifyPin: (String) -> Boolean,
    onSwitchAccount: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF060B19), Color(0xFF0F172A)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xE61E293B),
            border = BorderStroke(1.5.dp, TurquoiseAccent.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = TurquoiseAccent,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "بىخەتەرلىك ئىجازەت قۇلۇپى",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "«${user.username}» ھېساباتى قوغداش ھالىتىدە. داۋاملاشتۇرۇش ئۈچۈن ئىجازەت كودى ياكى مەخپىي نومۇرنى كىرگۈزۈڭ.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        error = null
                    },
                    label = { Text("ئىجازەت كودى / مەخپىي نومۇر") },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = TurquoiseAccent) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        color = RoseUnpaid,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (!onVerifyPin(code)) {
                            error = "ئىجازەت كودى خاتا! رۇخسەت قىلىنمىدى."
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("ئىجازەت بىلەن ئېچىش", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "باشقا ھېساباتقا ئالمىشىش",
                    style = MaterialTheme.typography.labelMedium,
                    color = TurquoiseAccent,
                    modifier = Modifier
                        .clickable { onSwitchAccount() }
                        .padding(6.dp)
                )
            }
        }
    }
}
