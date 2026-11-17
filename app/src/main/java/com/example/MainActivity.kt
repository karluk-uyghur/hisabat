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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

        // ئېكراننى رەسىمگە تارتىش، سىنغا ئېلىش ۋە باشقا ئۈسكۈنىلەرنىڭ ئىز قوغلىشىنى قاتتىق چەكلەش
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
    val passwordHash: String,
    val licenseCode: String
)

class LocalAuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("hesabat_secure_auth_prefs", Context.MODE_PRIVATE)

    fun verifyAdminPassword(inputPassword: String): Boolean {
        val savedAdminHash = prefs.getString(KEY_ADMIN_HASH, null)
        val defaultAdminHash = sha256(DEFAULT_ADMIN_PASSWORD)
        val activeHash = savedAdminHash ?: defaultAdminHash
        return sha256(inputPassword.trim()) == activeHash
    }

    fun updateAdminPassword(newPassword: String): Boolean {
        val clean = newPassword.trim()
        if (clean.length < 4) return false
        prefs.edit().putString(KEY_ADMIN_HASH, sha256(clean)).commit()
        return true
    }

    /**
     * ھەر بىر شەخسنىڭ ھېسابات ئىسمىغا ياكى Gmail ئادرېسىغا باغلاپ ئۆزگىچە ئىجازەت كودى ياساش.
     * 1-شەخسنىڭ كودى 2-شەخستە قەتئىي ئىشلىمەيدۇ.
     */
    fun generateLicenseForAccount(accountName: String): String {
        val normalized = accountName.trim().lowercase()
        if (normalized.isEmpty()) return ""
        val rawHash = sha256("HESABAT_MASTER_KEY_2026::$normalized").uppercase()
        val part1 = rawHash.substring(0, 4)
        val part2 = rawHash.substring(4, 8)
        return "HSB-$part1-$part2"
    }

    fun isLicenseValidForAccount(accountName: String, enteredCode: String): Boolean {
        val expected = generateLicenseForAccount(accountName)
        if (expected.isEmpty()) return false
        val cleanEntered = enteredCode.trim().uppercase()
        val compactEntered = cleanEntered.replace("-", "").replace(" ", "")
        val compactExpected = expected.replace("-", "")
        return cleanEntered == expected || compactEntered == compactExpected
    }

    fun verifyExternalGateCode(enteredCode: String): Boolean {
        val clean = enteredCode.trim()
        if (clean.isEmpty()) return false
        if (verifyAdminPassword(clean)) return true
        val active = getActiveUser()
        if (active != null && isLicenseValidForAccount(active.username, clean)) return true
        return getRegisteredUsers().any { isLicenseValidForAccount(it.username, clean) }
    }

    fun getRegisteredUsers(): List<LocalUserAccount> {
        val raw = prefs.getString(KEY_USERS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<LocalUserAccount>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val uname = obj.optString("username")
                list.add(
                    LocalUserAccount(
                        userId = obj.optString("userId"),
                        username = uname,
                        passwordHash = obj.optString("passwordHash"),
                        licenseCode = obj.optString("licenseCode").ifBlank { generateLicenseForAccount(uname) }
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

    fun registerOrLoginGoogleUser(
        googleEmailOrName: String,
        password: String,
        licenseCode: String
    ): Result<LocalUserAccount> {
        val cleanEmail = googleEmailOrName.trim()
        if (cleanEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Google ھېسابات نامى ياكى Gmail نى كىرگۈزۈڭ"))
        }
        val users = getRegisteredUsers().toMutableList()
        val existing = users.firstOrNull { it.username.equals(cleanEmail, ignoreCase = true) }

        if (existing != null) {
            val isLicenseMatch = isLicenseValidForAccount(existing.username, licenseCode)
            val isPasswordMatch = password.isNotEmpty() && existing.passwordHash == sha256(password)
            if (!isLicenseMatch && !isPasswordMatch) {
                return Result.failure(
                    IllegalArgumentException("بۇ Google ھېساباتىنىڭ مەخپىي نومۇرى ياكى ئىجازەت كودى خاتا!")
                )
            }
            prefs.edit().putString(KEY_ACTIVE_USER_ID, existing.userId).commit()
            return Result.success(existing)
        }

        if (!isLicenseValidForAccount(cleanEmail, licenseCode)) {
            return Result.failure(
                IllegalArgumentException("ئىجازەت كودى خاتا ياكى بۇ Google ھېساباتىغا تەۋە ئەمەس! باشقۇرغۇچىدىن مەخسۇس ئىجازەت كودى ئېلىڭ.")
            )
        }

        val effectivePassword = password.trim().ifEmpty { licenseCode.trim().uppercase() }
        val userId = "user_" + sha256(cleanEmail.lowercase()).take(16)
        val newAccount = LocalUserAccount(
            userId = userId,
            username = cleanEmail,
            passwordHash = sha256(effectivePassword),
            licenseCode = generateLicenseForAccount(cleanEmail)
        )
        users.add(newAccount)
        saveUsers(users)
        prefs.edit().putString(KEY_ACTIVE_USER_ID, newAccount.userId).commit()
        return Result.success(newAccount)
    }

    fun registerUser(
        username: String,
        password: String,
        licenseCode: String
    ): Result<LocalUserAccount> {
        val cleanName = username.trim()
        if (cleanName.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("ھېسابات ئىسمى ۋە مەخپىي نومۇرنى تولۇق كىرگۈزۈڭ"))
        }
        if (!isLicenseValidForAccount(cleanName, licenseCode)) {
            return Result.failure(
                IllegalArgumentException("ئىجازەت كودى خاتا! ھەر بىر ھېسابات ئىسمى ئۈچۈن باشقۇرغۇچى تەمىنلىگەن مەخسۇس ئىجازەت كودى كېرەك.")
            )
        }
        val users = getRegisteredUsers().toMutableList()
        val existing = users.firstOrNull { it.username.equals(cleanName, ignoreCase = true) }
        if (existing != null) {
            if (existing.passwordHash == sha256(password)) {
                prefs.edit().putString(KEY_ACTIVE_USER_ID, existing.userId).commit()
                return Result.success(existing)
            }
            return Result.failure(IllegalArgumentException("بۇ ھېسابات بۇرۇن ئېچىلغان، «ھېساباتقا كىرىش» تىن مەخپىي نومۇر بىلەن كىرىڭ"))
        }

        val userId = "user_" + sha256(cleanName.lowercase()).take(16)
        val account = LocalUserAccount(
            userId = userId,
            username = cleanName,
            passwordHash = sha256(password),
            licenseCode = generateLicenseForAccount(cleanName)
        )
        users.add(account)
        saveUsers(users)
        prefs.edit().putString(KEY_ACTIVE_USER_ID, account.userId).commit()
        return Result.success(account)
    }

    fun loginUser(
        username: String,
        password: String,
        licenseCode: String
    ): Result<LocalUserAccount> {
        val cleanName = username.trim()
        val users = getRegisteredUsers()
        val matched = users.firstOrNull { it.username.equals(cleanName, ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("بۇ ھېسابات تېپىلمىدى، ئالدى بىلەن ئىجازەت كودى ئارقىلىق يېڭى ھېسابات ئېچىڭ"))

        val passwordOk = matched.passwordHash == sha256(password)
        val licenseOk = licenseCode.isNotBlank() && isLicenseValidForAccount(matched.username, licenseCode)

        if (!passwordOk && !licenseOk) {
            return Result.failure(IllegalArgumentException("مەخپىي نومۇر خاتا!"))
        }
        prefs.edit().putString(KEY_ACTIVE_USER_ID, matched.userId).commit()
        return Result.success(matched)
    }

    fun deleteRegisteredUser(userId: String) {
        val updated = getRegisteredUsers().filterNot { it.userId == userId }
        saveUsers(updated)
        if (prefs.getString(KEY_ACTIVE_USER_ID, null) == userId) {
            prefs.edit().remove(KEY_ACTIVE_USER_ID).commit()
        }
    }

    fun verifyPassword(user: LocalUserAccount, password: String): Boolean {
        return user.passwordHash == sha256(password) || isLicenseValidForAccount(user.username, password)
    }

    fun logout() {
        prefs.edit().remove(KEY_ACTIVE_USER_ID).commit()
    }

    private fun saveUsers(users: List<LocalUserAccount>) {
        val arr = JSONArray()
        users.forEach { u ->
            arr.put(
                JSONObject().apply {
                    put("userId", u.userId)
                    put("username", u.username)
                    put("passwordHash", u.passwordHash)
                    put("licenseCode", u.licenseCode)
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
        const val KEY_ADMIN_HASH = "master_admin_hash_v1"
        const val DEFAULT_ADMIN_PASSWORD = "162142"
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
    val authManager = remember { LocalAuthManager(context.applicationContext) }
    val repository = remember { AccountRepository(context.applicationContext) }

    var registeredUsers by remember { mutableStateOf(authManager.getRegisteredUsers()) }
    var currentUser by remember { mutableStateOf(authManager.getActiveUser()) }
    var needExternalPermission by remember { mutableStateOf(isExternalAccess) }
    var manualLockActive by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showAdminGeneratorDialog by remember { mutableStateOf(false) }

    if (needExternalPermission) {
        ExternalProgramPermissionDialog(
            callerName = externalCallerName,
            onVerifyCode = { enteredCode: String ->
                if (authManager.verifyExternalGateCode(enteredCode)) {
                    needExternalPermission = false
                    true
                } else {
                    false
                }
            },
            onDeny = onRejectExternal
        )
    }

    if (showGoogleDialog) {
        GoogleAccountAuthDialog(
            registeredUsers = registeredUsers,
            onDismiss = { showGoogleDialog = false },
            onSubmitGoogleAuth = { emailOrName: String, password: String, licenseCode: String ->
                val result = authManager.registerOrLoginGoogleUser(emailOrName, password, licenseCode)
                result.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                    showGoogleDialog = false
                }
                result.map { Unit }
            }
        )
    }

    if (showAdminGeneratorDialog) {
        AdminLicenseGeneratorDialog(
            authManager = authManager,
            registeredUsers = registeredUsers,
            onUsersChanged = {
                registeredUsers = authManager.getRegisteredUsers()
                currentUser = authManager.getActiveUser()
            },
            onDismiss = { showAdminGeneratorDialog = false }
        )
    }

    val activeAccount = currentUser
    if (activeAccount == null) {
        AccountPortalScreen(
            registeredUsers = registeredUsers,
            onLogin = { username: String, password: String, license: String ->
                val res = authManager.loginUser(username, password, license)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                }
                res.map { Unit }
            },
            onRegister = { username: String, password: String, license: String ->
                val res = authManager.registerUser(username, password, license)
                res.onSuccess { user ->
                    registeredUsers = authManager.getRegisteredUsers()
                    currentUser = user
                }
                res.map { Unit }
            },
            onGoogleSignInClick = {
                showGoogleDialog = true
            },
            onOpenAdminGenerator = {
                showAdminGeneratorDialog = true
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
                onOpenAdminPanel = {
                    manualLockActive = false
                    showAdminGeneratorDialog = true
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
private fun AdminLicenseGeneratorDialog(
    authManager: LocalAuthManager,
    registeredUsers: List<LocalUserAccount>,
    onUsersChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminError by remember { mutableStateOf<String?>(null) }

    var targetUsername by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }

    var showChangeAdminPwd by remember { mutableStateOf(false) }
    var newAdminPwd by remember { mutableStateOf("") }
    var pwdChangedMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = EmeraldPaid,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (isAdminUnlocked) "ئىجازەت كودى ياساش مەركىزى" else "باشقۇرغۇچى دەلىللەش",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            if (!isAdminUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "بۇ بۆلۈمگە پەقەت ئەپ ئىگىسىلا كىرەلەيدۇ. باشقۇرغۇچى مەخپىي نومۇرىنى كىرگۈزۈڭ:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = {
                            adminPasswordInput = it
                            adminError = null
                        },
                        label = { Text("باشقۇرغۇچى مەخپىي نومۇرى") },
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = IndigoAccent) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminError != null) {
                        Text(
                            text = adminError.orEmpty(),
                            color = RoseUnpaid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ئىشلەتكۈچىنىڭ ھېسابات ئىسمى ياكى Gmail ئادرېسىنى يازسىڭىز، پەقەت شۇ كىشىنىڭ شۇ ھېساباتىغىلا ئىشلەيدىغان مەخسۇس ئىجازەت كودى ياسىلىدۇ:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = targetUsername,
                        onValueChange = {
                            targetUsername = it
                            generatedCode = authManager.generateLicenseForAccount(it)
                            copiedNotice = false
                        },
                        label = { Text("ھېسابات ئىسمى ياكى Gmail") },
                        placeholder = { Text("مەسىلەن: alim ياكى alim@gmail.com") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = TurquoiseAccent) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (generatedCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = EmeraldPaid.copy(alpha = 0.16f),
                            border = BorderStroke(1.5.dp, EmeraldPaid),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "«${targetUsername.trim()}» نىڭ مەخسۇس ئىجازەت كودى:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = generatedCode,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPaid
                                )
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(generatedCode))
                                        copiedNotice = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (copiedNotice) "كۆچۈرۈلدى!" else "ئىجازەت كودىنى كۆچۈرۈش", color = Color.White)
                                }
                            }
                        }
                    }

                    if (registeredUsers.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "ئىجازەت بېرىلگەن ھېساباتلار (${registeredUsers.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        registeredUsers.forEach { acc ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(acc.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("كودى: ${acc.licenseCode}", style = MaterialTheme.typography.labelSmall, color = TurquoiseAccent)
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(acc.licenseCode))
                                            }
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                authManager.deleteRegisteredUser(acc.userId)
                                                onUsersChanged()
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, tint = RoseUnpaid, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                    Text(
                        text = if (showChangeAdminPwd) "باشقۇرغۇچى مەخپىي نومۇرىنى يېڭىلاش:" else "⚙ باشقۇرغۇچى مەخپىي نومۇرىنى ئۆزگەرتىش",
                        style = MaterialTheme.typography.labelMedium,
                        color = TurquoiseAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showChangeAdminPwd = !showChangeAdminPwd }
                    )

                    AnimatedVisibility(visible = showChangeAdminPwd) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newAdminPwd,
                                onValueChange = { newAdminPwd = it },
                                label = { Text("يېڭى باشقۇرغۇچى مەخپىي نومۇرى") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (authManager.updateAdminPassword(newAdminPwd)) {
                                        pwdChangedMsg = "باشقۇرغۇچى مەخپىي نومۇرى مۇۋەپپەقىيەتلىك ئۆزگەرتىلدى!"
                                        newAdminPwd = ""
                                    } else {
                                        pwdChangedMsg = "كەم دېگەندە 4 خانىلىق نومۇر كىرگۈزۈڭ"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ساقلاش", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            if (pwdChangedMsg != null) {
                                Text(pwdChangedMsg.orEmpty(), style = MaterialTheme.typography.labelSmall, color = EmeraldPaid)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isAdminUnlocked) {
                Button(
                    onClick = {
                        if (authManager.verifyAdminPassword(adminPasswordInput)) {
                            isAdminUnlocked = true
                            adminError = null
                        } else {
                            adminError = "باشقۇرغۇچى مەخپىي نومۇرى خاتا!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                ) {
                    Text("دەلىللەپ كىرىش", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                ) {
                    Text("تاقاش", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isAdminUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text("بىكار قىلىش")
                }
            }
        }
    )
}

@Composable
private fun GoogleAccountAuthDialog(
    registeredUsers: List<LocalUserAccount>,
    onDismiss: () -> Unit,
    onSubmitGoogleAuth: (String, String, String) -> Result<Unit>
) {
    var googleEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var licenseCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val isExistingGoogleUser = remember(googleEmail, registeredUsers) {
        registeredUsers.any { it.username.equals(googleEmail.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = TurquoiseAccent,
                modifier = Modifier.size(42.dp)
            )
        },
        title = {
            Text("Google ھېساباتى بىلەن كىرىش", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Gmail ئادرېسىڭىزنى ۋە شۇ ھېساباتقا بېرىلگەن مەخسۇس ئىجازەت كودىنى كىرگۈزۈڭ. مەلۇماتلىرىڭىز مەڭگۈلۈك ساقلىنىدۇ:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = googleEmail,
                    onValueChange = {
                        googleEmail = it
                        errorMsg = null
                    },
                    label = { Text("Gmail ئادرېسى ياكى Google نامى *") },
                    placeholder = { Text("example@gmail.com") },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = TurquoiseAccent) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = licenseCode,
                    onValueChange = {
                        licenseCode = it.uppercase()
                        errorMsg = null
                    },
                    label = {
                        Text(if (isExistingGoogleUser) "ئىجازەت كودى (ياكى مەخپىي نومۇر)" else "مەخسۇس ئىجازەت كودى (HSB-XXXX-XXXX) *")
                    },
                    placeholder = { Text("HSB-XXXX-XXXX") },
                    leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null, tint = EmeraldPaid) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMsg = null
                    },
                    label = { Text("مەخپىي نومۇر بېكىتىش (ئىختىيارىي)") },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = IndigoAccent) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg.orEmpty(),
                        color = RoseUnpaid,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val res = onSubmitGoogleAuth(googleEmail, password, licenseCode)
                    res.onFailure { err -> errorMsg = err.message }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) {
                Text("دەلىللەپ كىرىش", color = Color.White, fontWeight = FontWeight.Bold)
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
    onVerifyCode: (String) -> Boolean,
    onDeny: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                    text = "«$callerName» بۇ ئەپكە ئۇلانماقچى بولدى. باشقۇرغۇچى تەمىنلىگەن رەسمىي ئىجازەت كودىنى كىرگۈزمىگۈچە كىرىشكە رۇخسەت قىلىنمايدۇ:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        errorMsg = null
                    },
                    label = { Text("رەسمىي ئىجازەت كودى *") },
                    placeholder = { Text("HSB-XXXX-XXXX") },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = EmeraldPaid) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg.orEmpty(),
                        color = RoseUnpaid,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!onVerifyCode(code)) {
                        errorMsg = "ئىجازەت كودى خاتا! خالىغانچە كىرىشكە رۇخسەت يوق."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPaid)
            ) {
                Text("دەلىللەپ ئىجازەت بېرىش", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDeny,
                colors = ButtonDefaults.buttonColors(containerColor = RoseUnpaid)
            ) {
                Text("رەت قىلىپ چېكىنىش", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PasswordPermissionDialog(
    username: String,
    onVerify: (String) -> Boolean,
    onOpenAdminPanel: () -> Unit,
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
                Text("«$username» ھېساباتىنى ئېچىش ئۈچۈن مەخپىي نومۇر ياكى ئىجازەت كودىنى كىرگۈزۈڭ:")
                OutlinedTextField(
                    value = pwd,
                    onValueChange = {
                        pwd = it
                        err = null
                    },
                    label = { Text("مەخپىي نومۇر ياكى ئىجازەت كودى") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (err != null) {
                    Text(err.orEmpty(), color = RoseUnpaid, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "🔑 باشقۇرغۇچى ئىجازەت كودى ياساش مەركىزى",
                    style = MaterialTheme.typography.labelMedium,
                    color = TurquoiseAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenAdminPanel() }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!onVerify(pwd)) {
                        err = "مەخپىي نومۇر ياكى ئىجازەت كودى خاتا!"
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
    onLogin: (String, String, String) -> Result<Unit>,
    onRegister: (String, String, String) -> Result<Unit>,
    onGoogleSignInClick: () -> Unit,
    onOpenAdminGenerator: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(registeredUsers.isEmpty()) }
    var username by remember { mutableStateOf(registeredUsers.firstOrNull()?.username.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var licenseCode by remember { mutableStateOf("") }
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
                    // باشقۇرغۇچى ئىجازەت كودى ياساش كۇنۇپكىسى (پەقەت سىزلا مەخپىي نومۇر بىلەن كىرەلەيسىز)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoAccent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.45f)),
                            modifier = Modifier.clickable { onOpenAdminGenerator() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(15.dp))
                                Text(
                                    text = "ئىجازەت كودى ياساش (باشقۇرغۇچى)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TurquoiseAccent
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))),
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
                                text = "ھەر بىر كىشىگە مەخسۇس ئىجازەت كودى بىلەن قوغدالغان",
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
                                            errorMessage = null
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

                    AnimatedVisibility(visible = isRegisterMode) {
                        OutlinedTextField(
                            value = licenseCode,
                            onValueChange = {
                                licenseCode = it.uppercase()
                                errorMessage = null
                            },
                            label = { Text("شەخسىي ئىجازەت كودى (HSB-XXXX-XXXX) *") },
                            placeholder = { Text("HSB-XXXX-XXXX") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                    .background(EmeraldPaid.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Security, contentDescription = null, tint = EmeraldPaid, modifier = Modifier.size(18.dp))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
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
                            val res = if (isRegisterMode) {
                                onRegister(username, password, licenseCode)
                            } else {
                                onLogin(username, password, licenseCode)
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
                            text = if (isRegisterMode) "ئىجازەت كودى بىلەن ھېسابات ئېچىش" else "ھېساباتقا كىرىش",
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
