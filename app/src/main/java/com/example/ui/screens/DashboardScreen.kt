package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AccountRecord
import com.example.ui.components.AccountCard
import com.example.ui.components.AccountFormSheet
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.RoseUnpaid
import com.example.ui.theme.TurquoiseAccent
import com.example.ui.viewmodel.AccountFilter
import com.example.ui.viewmodel.AccountSort
import com.example.ui.viewmodel.AccountViewModel
import com.example.ui.viewmodel.AccountsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

enum class SearchTargetField(val title: String, val icon: ImageVector) {
    ALL("ھەممىسى", Icons.Filled.Search),
    NAME("ئىسىم", Icons.Filled.Person),
    LOCATION("ئورۇن (كوچا/ئۆي)", Icons.Filled.LocationOn),
    ACCOUNT("ھېسابات نومۇرى", Icons.Filled.Badge)
}

@Composable
fun DashboardScreen(
    viewModel: AccountViewModel,
    isDarkGlass: Boolean,
    onToggleTheme: () -> Unit,
    currentUsername: String = "",
    isAdminUser: Boolean = true,
    onOpenAdminCenter: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val rawState by viewModel.rawAccountsState.collectAsStateWithLifecycle()
    val filteredState by viewModel.filteredAccountsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var showDataMenu by remember { mutableStateOf(false) }
    var showAdminAnalytics by remember { mutableStateOf(true) }
    var showFormSheet by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountRecord?>(null) }
    var pendingDeleteAccount by remember { mutableStateOf<AccountRecord?>(null) }

    var isSearchPageOpen by remember { mutableStateOf(false) }
    var dedicatedSearchQuery by remember { mutableStateOf("") }
    var dedicatedSearchTarget by remember { mutableStateOf(SearchTargetField.ALL) }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportFailedMsg = stringResource(R.string.export_failed)
    val importFailedMsg = stringResource(R.string.import_failed)
    val savedMsg = stringResource(R.string.operation_success_saved)
    val updatedMsg = stringResource(R.string.operation_success_updated)
    val deletedMsg = stringResource(R.string.operation_success_deleted)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val json = viewModel.buildBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                scope.launch { snackbarHostState.showSnackbar(exportSuccessMsg) }
            } catch (_: Exception) {
                scope.launch { snackbarHostState.showSnackbar(exportFailedMsg) }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                } ?: ""
                viewModel.importBackupJson(
                    jsonString = jsonText,
                    onSuccess = { count ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.import_success, count)
                            )
                        }
                    },
                    onError = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            } catch (_: Exception) {
                scope.launch { snackbarHostState.showSnackbar(importFailedMsg) }
            }
        }
    }

    val allAccounts = (rawState as? AccountsUiState.Success)?.accounts ?: emptyList()
    val totalCount = allAccounts.size
    val fullyPaidCount = allAccounts.count { it.paidMonthsCount() == 12 }
    val totalPaidMonths = allAccounts.sumOf { it.paidMonthsCount() }
    val totalPossibleMonths = (totalCount * 12).coerceAtLeast(1)
    val totalUnpaidMonths = (totalCount * 12 - totalPaidMonths).coerceAtLeast(0)
    val collectionRatePercent = if (totalCount == 0) 0 else ((totalPaidMonths * 100f) / totalPossibleMonths).toInt()
    val collectionFraction = if (totalCount == 0) 0f else (totalPaidMonths.toFloat() / totalPossibleMonths.toFloat()).coerceIn(0f, 1f)

    val streetStats = remember(allAccounts) {
        allAccounts
            .filter { it.streetName.isNotBlank() }
            .groupBy { it.streetName.trim() }
            .map { (street, list) -> street to list.size }
            .sortedByDescending { it.second }
    }

    val borderColor = if (isDarkGlass) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f)
    val scrimColors = if (isDarkGlass) {
        listOf(Color(0x66060E1F), Color(0x990A162E))
    } else {
        listOf(Color(0x22FFFFFF), Color(0x44E2E8F0))
    }

    val copyHandler: (String, String) -> Unit = { label, value ->
        clipboardManager.setText(AnnotatedString(value))
        scope.launch {
            snackbarHostState.showSnackbar(
                context.getString(R.string.copied_to_clipboard, label)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_landscape),
            contentDescription = stringResource(R.string.background_image_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = scrimColors))
        )

        if (isSearchPageOpen) {
            BackHandler { isSearchPageOpen = false }
            DedicatedSearchPage(
                allAccounts = allAccounts,
                query = dedicatedSearchQuery,
                targetField = dedicatedSearchTarget,
                isDarkGlass = isDarkGlass,
                borderColor = borderColor,
                onQueryChange = { q: String -> dedicatedSearchQuery = q },
                onTargetChange = { t: SearchTargetField -> dedicatedSearchTarget = t },
                onBack = { isSearchPageOpen = false },
                onEditClick = { acc: AccountRecord ->
                    editingAccount = acc
                    showFormSheet = true
                },
                onDeleteClick = { acc: AccountRecord -> pendingDeleteAccount = acc },
                onToggleMonth = { acc: AccountRecord, mIdx: Int ->
                    viewModel.toggleMonth(acc, mIdx) { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                },
                onCopyText = copyHandler
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingAccount = null
                            showFormSheet = true
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = {
                            Text(
                                text = stringResource(R.string.add_account_fab),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        containerColor = IndigoAccent,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_account_fab")
                    )
                }
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val isWide = maxWidth >= 720.dp
                    val isCompact = maxWidth < 420.dp

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Glass Card (باشقۇرغۇچى مەخسۇس باش تاختىسى)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 960.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(28.dp)),
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // ماۋزۇ قۇرى (پۈتۈن كەڭلىكتە نورمال كۆرۈنىدۇ)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(IndigoAccent, TurquoiseAccent)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.app_title),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(R.string.app_subtitle),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // باشقۇرغۇچى بەلگىسى، كونترول مەركىزى ۋە قورال كۇنۇپكىلار قۇرى
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = EmeraldPaid.copy(alpha = 0.16f),
                                                border = BorderStroke(1.dp, EmeraldPaid.copy(alpha = 0.45f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                                        text = if (currentUsername.isNotBlank()) currentUsername else "باش باشقۇرغۇچى",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = IndigoAccent,
                                                modifier = Modifier.clickable { onOpenAdminCenter() }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Security,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = "باشقۇرغۇچى مەركىزى",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            IconButton(onClick = { showAdminAnalytics = !showAdminAnalytics }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Speed,
                                                    contentDescription = null,
                                                    tint = EmeraldPaid
                                                )
                                            }
                                            IconButton(
                                                onClick = onToggleTheme,
                                                modifier = Modifier.testTag("toggle_theme_button")
                                            ) {
                                                Icon(
                                                    imageVector = if (isDarkGlass) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                                    contentDescription = stringResource(R.string.toggle_theme_cd),
                                                    tint = if (isDarkGlass) Color(0xFFFBBF24) else IndigoAccent
                                                )
                                            }
                                            IconButton(
                                                onClick = { showDataMenu = !showDataMenu },
                                                modifier = Modifier.testTag("backup_menu_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Storage,
                                                    contentDescription = stringResource(R.string.backup_menu_cd),
                                                    tint = TurquoiseAccent
                                                )
                                            }
                                            IconButton(onClick = onLockApp) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    tint = EmeraldPaid
                                                )
                                            }
                                            IconButton(
                                                onClick = onSignOut,
                                                modifier = Modifier.testTag("sign_out_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                                    contentDescription = stringResource(R.string.sign_out),
                                                    tint = RoseUnpaid
                                                )
                                            }
                                        }
                                    }

                                    // Prominent Stats Badges Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = EmeraldPaid
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Group,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.total_accounts_label)} $totalCount",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = TurquoiseAccent.copy(alpha = 0.2f),
                                            modifier = Modifier.border(
                                                1.dp,
                                                TurquoiseAccent.copy(alpha = 0.45f),
                                                RoundedCornerShape(16.dp)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = TurquoiseAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.fully_paid_accounts_label)}: $fullyPaidCount",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = IndigoAccent.copy(alpha = 0.18f),
                                            modifier = Modifier.border(
                                                1.dp,
                                                IndigoAccent.copy(alpha = 0.4f),
                                                RoundedCornerShape(16.dp)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.DateRange,
                                                    contentDescription = null,
                                                    tint = IndigoAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "${stringResource(R.string.paid_months_summary_label)}: $totalPaidMonths",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = RoseUnpaid.copy(alpha = 0.16f),
                                            modifier = Modifier.border(
                                                1.dp,
                                                RoseUnpaid.copy(alpha = 0.4f),
                                                RoundedCornerShape(16.dp)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Warning,
                                                    contentDescription = null,
                                                    tint = RoseUnpaid,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "تۆلەنمىگەن ئاي: $totalUnpaidMonths",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoseUnpaid
                                                )
                                            }
                                        }
                                    }

                                    // باشقۇرغۇچى ئەقلىي ئانالىز ۋە دوكلات تاختىسى (يېڭى زامانىۋى ئىقتىدار)
                                    AnimatedVisibility(visible = showAdminAnalytics) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            border = BorderStroke(1.dp, borderColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Speed,
                                                            contentDescription = null,
                                                            tint = TurquoiseAccent,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = "ئومۇمىي پۇل يىغىش نىسبىتى: $collectionRatePercent%",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = IndigoAccent.copy(alpha = 0.2f),
                                                        border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.45f)),
                                                        modifier = Modifier.clickable {
                                                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                                                            val report = buildString {
                                                                appendLine("📊 ھېسابات باشقۇرغۇچى دوكلاتى ($dateStr)")
                                                                appendLine("• جەمئىي ھېساباتلار: $totalCount")
                                                                appendLine("• تولۇق تۆلىگەنلەر: $fullyPaidCount")
                                                                appendLine("• جەمئىي تۆلەنگەن ئاي: $totalPaidMonths")
                                                                appendLine("• جەمئىي تۆلەنمىگەن ئاي: $totalUnpaidMonths")
                                                                appendLine("• يىغىش نىسبىتى: $collectionRatePercent%")
                                                                if (streetStats.isNotEmpty()) {
                                                                    appendLine("• ئورۇنلار تەپسىلاتى: " + streetStats.joinToString("، ") { "${it.first} (${it.second})" })
                                                                }
                                                            }
                                                            clipboardManager.setText(AnnotatedString(report))
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("باشقۇرغۇچى دوكلاتى كۆچۈرۈلدى!")
                                                            }
                                                        }
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.ContentCopy,
                                                                contentDescription = null,
                                                                tint = TurquoiseAccent,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = "دوكلات چىقىرىش",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                // زامانىۋى گىرادىيېنت تۆلەش بالدىقى
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(10.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.12f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(collectionFraction)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(IndigoAccent, TurquoiseAccent, EmeraldPaid)
                                                                )
                                                            )
                                                    )
                                                }

                                                if (streetStats.isNotEmpty()) {
                                                    HorizontalDivider(color = borderColor)
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .horizontalScroll(rememberScrollState()),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "كوچا/ئورۇنلار:",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        streetStats.forEach { (street, count) ->
                                                            FilterChip(
                                                                selected = false,
                                                                onClick = {
                                                                    dedicatedSearchTarget = SearchTargetField.LOCATION
                                                                    dedicatedSearchQuery = street
                                                                    isSearchPageOpen = true
                                                                },
                                                                leadingIcon = {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.LocationOn,
                                                                        contentDescription = null,
                                                                        tint = RoseUnpaid,
                                                                        modifier = Modifier.size(15.dp)
                                                                    )
                                                                },
                                                                label = { Text("$street ($count)") }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Expandable JSON Backup / Restore Drawer
                                    AnimatedVisibility(visible = showDataMenu) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                                    exportLauncher.launch("hesabat_backup_$dateStamp.json")
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = IndigoAccent.copy(alpha = 0.2f),
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("export_json_button")
                                            ) {
                                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(R.string.export_backup),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                                },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = TurquoiseAccent.copy(alpha = 0.2f),
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("import_json_button")
                                            ) {
                                                Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(R.string.import_backup),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Search & Filter Glass Card (ئىزدىگەندە يېڭى بەت ئېچىلىدۇ)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 960.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.updateSearchQuery(it) },
                                        placeholder = {
                                            Text(
                                                text = stringResource(R.string.search_placeholder),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))
                                                    )
                                                    .clickable {
                                                        dedicatedSearchQuery = searchQuery
                                                        dedicatedSearchTarget = SearchTargetField.ALL
                                                        isSearchPageOpen = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Search,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (searchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Close,
                                                            contentDescription = stringResource(R.string.clear_search_cd)
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = IndigoAccent,
                                                    modifier = Modifier
                                                        .padding(end = 6.dp)
                                                        .clickable {
                                                            dedicatedSearchQuery = searchQuery
                                                            dedicatedSearchTarget = SearchTargetField.ALL
                                                            isSearchPageOpen = true
                                                        }
                                                ) {
                                                    Text(
                                                        text = "ئىزدەش",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                dedicatedSearchQuery = searchQuery
                                                dedicatedSearchTarget = SearchTargetField.ALL
                                                isSearchPageOpen = true
                                            }
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = IndigoAccent,
                                            unfocusedBorderColor = borderColor
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_input")
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SearchTargetField.entries.forEach { target ->
                                            FilterChip(
                                                selected = false,
                                                onClick = {
                                                    dedicatedSearchTarget = target
                                                    dedicatedSearchQuery = searchQuery
                                                    isSearchPageOpen = true
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = target.icon,
                                                        contentDescription = null,
                                                        tint = TurquoiseAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                label = { Text(target.title) }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = selectedFilter == AccountFilter.ALL,
                                            onClick = { viewModel.updateFilter(AccountFilter.ALL) },
                                            label = { Text(stringResource(R.string.filter_all)) }
                                        )
                                        FilterChip(
                                            selected = selectedFilter == AccountFilter.HAS_UNPAID,
                                            onClick = { viewModel.updateFilter(AccountFilter.HAS_UNPAID) },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Warning, contentDescription = null, tint = RoseUnpaid, modifier = Modifier.size(16.dp))
                                            },
                                            label = { Text(stringResource(R.string.filter_unpaid)) }
                                        )
                                        FilterChip(
                                            selected = selectedFilter == AccountFilter.FULLY_PAID,
                                            onClick = { viewModel.updateFilter(AccountFilter.FULLY_PAID) },
                                            leadingIcon = {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldPaid, modifier = Modifier.size(16.dp))
                                            },
                                            label = { Text(stringResource(R.string.filter_fully_paid)) }
                                        )
                                        FilterChip(
                                            selected = selectedSort != AccountSort.NEWEST,
                                            onClick = {
                                                val next = when (selectedSort) {
                                                    AccountSort.NEWEST -> AccountSort.NAME
                                                    AccountSort.NAME -> AccountSort.PAID_PROGRESS
                                                    AccountSort.PAID_PROGRESS -> AccountSort.NEWEST
                                                }
                                                viewModel.updateSort(next)
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Sort, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(16.dp))
                                            },
                                            label = {
                                                Text(
                                                    stringResource(
                                                        when (selectedSort) {
                                                            AccountSort.NEWEST -> R.string.sort_newest
                                                            AccountSort.NAME -> R.string.sort_name
                                                            AccountSort.PAID_PROGRESS -> R.string.sort_progress
                                                        }
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Accounts Content State
                        when (val state = filteredState) {
                            is AccountsUiState.Loading -> {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(max = 960.dp)
                                            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(36.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            CircularProgressIndicator()
                                            Text(
                                                text = stringResource(R.string.loading_accounts),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }

                            is AccountsUiState.Error -> {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .widthIn(max = 960.dp)
                                            .border(1.dp, RoseUnpaid.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = RoseUnpaid,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            is AccountsUiState.Success -> {
                                val accounts = state.accounts
                                if (accounts.isEmpty()) {
                                    item {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .widthIn(max = 960.dp)
                                                .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                                            shape = RoundedCornerShape(24.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(36.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.FolderOpen,
                                                    contentDescription = null,
                                                    tint = IndigoAccent.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(52.dp)
                                                )
                                                Text(
                                                    text = if (searchQuery.isNotBlank()) {
                                                        stringResource(R.string.empty_search_message)
                                                    } else {
                                                        stringResource(R.string.empty_accounts_title)
                                                    },
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                                if (searchQuery.isBlank()) {
                                                    Text(
                                                        text = stringResource(R.string.empty_accounts_subtitle),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .widthIn(max = 960.dp)
                                                .padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (searchQuery.isNotBlank()) {
                                                    stringResource(R.string.search_results_count, accounts.size)
                                                } else {
                                                    stringResource(R.string.saved_accounts_section)
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    if (isWide) {
                                        val paired = accounts.chunked(2)
                                        items(paired, key = { pair -> pair.joinToString("_") { it.id } }) { pair ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .widthIn(max = 960.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                pair.forEach { account ->
                                                    AccountCard(
                                                        account = account,
                                                        isDarkGlass = isDarkGlass,
                                                        isCompactWidth = true,
                                                        onEditClick = {
                                                            editingAccount = account
                                                            showFormSheet = true
                                                        },
                                                        onDeleteClick = { pendingDeleteAccount = account },
                                                        onToggleMonth = { mIdx ->
                                                            viewModel.toggleMonth(account, mIdx) { err ->
                                                                scope.launch { snackbarHostState.showSnackbar(err) }
                                                            }
                                                        },
                                                        onCopyText = copyHandler,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (pair.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    } else {
                                        items(accounts, key = { it.id }) { account ->
                                            AccountCard(
                                                account = account,
                                                isDarkGlass = isDarkGlass,
                                                isCompactWidth = isCompact,
                                                onEditClick = {
                                                    editingAccount = account
                                                    showFormSheet = true
                                                },
                                                onDeleteClick = { pendingDeleteAccount = account },
                                                onToggleMonth = { mIdx ->
                                                    viewModel.toggleMonth(account, mIdx) { err ->
                                                        scope.launch { snackbarHostState.showSnackbar(err) }
                                                    }
                                                },
                                                onCopyText = copyHandler,
                                                modifier = Modifier.widthIn(max = 680.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        AccountFormSheet(
            editingAccount = editingAccount,
            isSaving = isSaving,
            onDismiss = {
                if (!isSaving) {
                    showFormSheet = false
                    editingAccount = null
                }
            },
            onSubmit = { editingId, name, accountName, password, flowAmount, openDate, streetName, houseNumber, notes, months ->
                viewModel.saveAccount(
                    editingId = editingId,
                    name = name,
                    accountName = accountName,
                    password = password,
                    flowAmount = flowAmount,
                    openDate = openDate,
                    streetName = streetName,
                    houseNumber = houseNumber,
                    notes = notes,
                    months = months,
                    onSuccess = { wasEdit ->
                        showFormSheet = false
                        editingAccount = null
                        scope.launch {
                            snackbarHostState.showSnackbar(if (wasEdit) updatedMsg else savedMsg)
                        }
                    },
                    onError = { err ->
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                )
            }
        )
    }

    pendingDeleteAccount?.let { accountToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteAccount = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = RoseUnpaid
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_dialog_message, accountToDelete.name),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = accountToDelete.id
                        pendingDeleteAccount = null
                        viewModel.deleteAccount(
                            accountId = idToDelete,
                            onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar(deletedMsg) }
                            },
                            onError = { err ->
                                scope.launch { snackbarHostState.showSnackbar(err) }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUnpaid),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(stringResource(R.string.delete_action), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteAccount = null },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text(stringResource(R.string.cancel_action))
                }
            }
        )
    }
}

@Composable
private fun DedicatedSearchPage(
    allAccounts: List<AccountRecord>,
    query: String,
    targetField: SearchTargetField,
    isDarkGlass: Boolean,
    borderColor: Color,
    onQueryChange: (String) -> Unit,
    onTargetChange: (SearchTargetField) -> Unit,
    onBack: () -> Unit,
    onEditClick: (AccountRecord) -> Unit,
    onDeleteClick: (AccountRecord) -> Unit,
    onToggleMonth: (AccountRecord, Int) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val q = query.trim().lowercase()
    val results = remember(allAccounts, q, targetField) {
        if (q.isEmpty()) {
            allAccounts
        } else {
            allAccounts.filter { r ->
                when (targetField) {
                    SearchTargetField.ALL ->
                        r.name.lowercase().contains(q) ||
                            r.accountName.lowercase().contains(q) ||
                            r.streetName.lowercase().contains(q) ||
                            r.houseNumber.lowercase().contains(q) ||
                            r.flowAmount.lowercase().contains(q) ||
                            r.notes.lowercase().contains(q)
                    SearchTargetField.NAME -> r.name.lowercase().contains(q)
                    SearchTargetField.LOCATION ->
                        r.streetName.lowercase().contains(q) || r.houseNumber.lowercase().contains(q)
                    SearchTargetField.ACCOUNT -> r.accountName.lowercase().contains(q)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 960.dp)
                        .border(1.5.dp, borderColor, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IndigoAccent,
                                modifier = Modifier.clickable { onBack() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "قايتىش",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ئىزدەش نەتىجە بېتى (${results.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ئىسىم، ئورۇن ياكى ھېسابات بويىچە ئايرىم بەتتە باشقۇرۇش",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            placeholder = { Text("ئىسىم، ئورۇن (كوچا/ئۆي) ياكى ھېسابات نومۇرىنى يېزىڭ...") },
                            leadingIcon = {
                                Icon(targetField.icon, contentDescription = null, tint = TurquoiseAccent)
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SearchTargetField.entries.forEach { target ->
                                FilterChip(
                                    selected = targetField == target,
                                    onClick = { onTargetChange(target) },
                                    leadingIcon = {
                                        Icon(target.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    label = { Text(target.title) }
                                )
                            }
                        }
                    }
                }
            }

            if (results.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 960.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = TurquoiseAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = stringResource(R.string.empty_search_message),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(results, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        isDarkGlass = isDarkGlass,
                        isCompactWidth = false,
                        onEditClick = { onEditClick(account) },
                        onDeleteClick = { onDeleteClick(account) },
                        onToggleMonth = { mIdx -> onToggleMonth(account, mIdx) },
                        onCopyText = onCopyText,
                        modifier = Modifier.widthIn(max = 680.dp)
                    )
                }
            }
        }
    }
}
