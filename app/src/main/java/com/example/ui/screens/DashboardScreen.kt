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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
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
    NAME("ئىسىم بويىچە", Icons.Filled.Person),
    LOCATION("ئورۇن (كوچا/ئۆي)", Icons.Filled.LocationOn),
    ACCOUNT("ھېسابات نومۇرى", Icons.Filled.Badge)
}

@Composable
fun StatBadgePill(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    contentColor: Color,
    borderColor: Color = Color.Transparent
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun TermIconChip(icon: ImageVector, label: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: AccountViewModel,
    isDarkGlass: Boolean,
    onToggleTheme: () -> Unit,
    currentUsername: String = "",
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
    var showTermsGuide by remember { mutableStateOf(false) }
    var showFormSheet by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountRecord?>(null) }
    var pendingDeleteAccount by remember { mutableStateOf<AccountRecord?>(null) }

    var isSearchPageOpen by remember { mutableStateOf(false) }
    var searchPageQuery by remember { mutableStateOf("") }
    var searchTargetField by remember { mutableStateOf(SearchTargetField.ALL) }

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
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
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
                val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                viewModel.importBackupJson(
                    jsonString = jsonText,
                    onSuccess = { count ->
                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.import_success, count)) }
                    },
                    onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
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
    val totalUnpaidMonths = (totalCount * 12 - totalPaidMonths).coerceAtLeast(0)

    val borderColor = if (isDarkGlass) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f)
    val scrimColors = if (isDarkGlass) listOf(Color(0x99060E1F), Color(0xCC0A162E)) else listOf(Color(0x33FFFFFF), Color(0x66E2E8F0))

    val copyHandler: (String, String) -> Unit = { label, value ->
        clipboardManager.setText(AnnotatedString(value))
        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.copied_to_clipboard, label)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_landscape),
            contentDescription = stringResource(R.string.background_image_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = scrimColors)))

        if (isSearchPageOpen) {
            BackHandler { isSearchPageOpen = false }
            SearchResultsDedicatedPage(
                allAccounts = allAccounts,
                initialQuery = searchPageQuery,
                selectedTarget = searchTargetField,
                isDarkGlass = isDarkGlass,
                borderColor = borderColor,
                onQueryChange = { q: String -> searchPageQuery = q },
                onTargetChange = { t: SearchTargetField -> searchTargetField = t },
                onBack = { isSearchPageOpen = false },
                onEditClick = { acc: AccountRecord ->
                    editingAccount = acc
                    showFormSheet = true
                },
                onDeleteClick = { acc: AccountRecord -> pendingDeleteAccount = acc },
                onToggleMonth = { acc: AccountRecord, mIdx: Int ->
                    viewModel.toggleMonth(acc, mIdx) { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
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
                        text = { Text(stringResource(R.string.add_account_fab), fontWeight = FontWeight.Bold) },
                        containerColor = IndigoAccent,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_account_fab")
                    )
                }
            ) { innerPadding ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    val isCompact = maxWidth < 420.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp).border(1.dp, borderColor, RoundedCornerShape(28.dp)),
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(6.dp).background(
                                            Brush.horizontalGradient(listOf(IndigoAccent, TurquoiseAccent, EmeraldPaid))
                                        )
                                    )
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(
                                                        Brush.linearGradient(listOf(IndigoAccent, TurquoiseAccent))
                                                    ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                                }
                                                Column {
                                                    Text(stringResource(R.string.app_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Icon(Icons.Filled.Shield, contentDescription = null, tint = EmeraldPaid, modifier = Modifier.size(14.dp))
                                                        Text(
                                                            text = if (currentUsername.isNotBlank()) "ھېسابات: $currentUsername • قوغدالغان" else stringResource(R.string.app_subtitle),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                IconButton(onClick = { showTermsGuide = !showTermsGuide }) {
                                                    Icon(Icons.Filled.Info, contentDescription = null, tint = TurquoiseAccent)
                                                }
                                                IconButton(onClick = onToggleTheme) {
                                                    Icon(
                                                        imageVector = if (isDarkGlass) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                                        contentDescription = stringResource(R.string.toggle_theme_cd),
                                                        tint = if (isDarkGlass) Color(0xFFFBBF24) else IndigoAccent
                                                    )
                                                }
                                                IconButton(onClick = { showDataMenu = !showDataMenu }) {
                                                    Icon(Icons.Filled.Storage, contentDescription = stringResource(R.string.backup_menu_cd))
                                                }
                                                IconButton(onClick = onLockApp) {
                                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = EmeraldPaid)
                                                }
                                                IconButton(onClick = onSignOut) {
                                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.sign_out), tint = RoseUnpaid)
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            StatBadgePill(Icons.Filled.Group, "${stringResource(R.string.total_accounts_label)} $totalCount", IndigoAccent, Color.White)
                                            StatBadgePill(Icons.Filled.CheckCircle, "${stringResource(R.string.fully_paid_accounts_label)}: $fullyPaidCount", EmeraldPaid.copy(alpha = 0.2f), EmeraldPaid, EmeraldPaid.copy(alpha = 0.45f))
                                            StatBadgePill(Icons.Filled.DateRange, "${stringResource(R.string.paid_months_summary_label)}: $totalPaidMonths", TurquoiseAccent.copy(alpha = 0.2f), TurquoiseAccent, TurquoiseAccent.copy(alpha = 0.45f))
                                            StatBadgePill(Icons.Filled.Warning, "تۆلەنمىگەن: $totalUnpaidMonths", RoseUnpaid.copy(alpha = 0.18f), RoseUnpaid, RoseUnpaid.copy(alpha = 0.45f))
                                        }

                                        AnimatedVisibility(visible = showTermsGuide) {
                                            Surface(
                                                shape = RoundedCornerShape(18.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                                border = BorderStroke(1.dp, borderColor),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("ئاتالغۇلارغا خاس سىنبەلگىلەر:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        TermIconChip(Icons.Filled.Person, "ئىسىم", IndigoAccent)
                                                        TermIconChip(Icons.Filled.Badge, "ھېسابات نومۇرى", TurquoiseAccent)
                                                        TermIconChip(Icons.Filled.Key, "مەخپىي نومۇر", Color(0xFFF59E0B))
                                                        TermIconChip(Icons.Filled.Speed, "ئېقىم مىقدارى", EmeraldPaid)
                                                        TermIconChip(Icons.Filled.DateRange, "ئاچقان ۋاقتى", IndigoAccent)
                                                        TermIconChip(Icons.Filled.LocationOn, "كوچا ئىسمى", RoseUnpaid)
                                                        TermIconChip(Icons.Filled.Home, "ئۆي نومۇرى", TurquoiseAccent)
                                                        TermIconChip(Icons.Filled.Info, "ئىزاھات", EmeraldPaid)
                                                        TermIconChip(Icons.Filled.Shield, "مەخپىيەتلىك قوغداش", EmeraldPaid)
                                                    }
                                                }
                                            }
                                        }

                                        AnimatedVisibility(visible = showDataMenu) {
                                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Button(
                                                    onClick = {
                                                        val dateStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                                        exportLauncher.launch("hesabat_backup_$dateStamp.json")
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.onSurface),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.export_backup), style = MaterialTheme.typography.labelMedium)
                                                }
                                                Button(
                                                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseAccent.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.onSurface),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(stringResource(R.string.import_backup), style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp).border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { viewModel.updateSearchQuery(it) },
                                            placeholder = { Text("ئىسىم، ئورۇن (كوچا/ئۆي) ياكى ھېسابات...", style = MaterialTheme.typography.bodyMedium) },
                                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                            trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search_cd))
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(
                                                onSearch = {
                                                    searchPageQuery = searchQuery
                                                    isSearchPageOpen = true
                                                }
                                            ),
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoAccent, unfocusedBorderColor = borderColor),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                searchPageQuery = searchQuery
                                                isSearchPageOpen = true
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                                        ) {
                                            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ئىزدەش بېتى", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SearchTargetField.entries.forEach { target ->
                                            FilterChip(
                                                selected = searchTargetField == target && isSearchPageOpen,
                                                onClick = {
                                                    searchTargetField = target
                                                    searchPageQuery = searchQuery
                                                    isSearchPageOpen = true
                                                },
                                                leadingIcon = { Icon(target.icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                label = { Text(target.title) }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
                                            label = { Text(stringResource(R.string.filter_unpaid)) }
                                        )
                                        FilterChip(
                                            selected = selectedFilter == AccountFilter.FULLY_PAID,
                                            onClick = { viewModel.updateFilter(AccountFilter.FULLY_PAID) },
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

                        when (val state = filteredState) {
                            is AccountsUiState.Loading -> {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(modifier = Modifier.padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(stringResource(R.string.loading_accounts))
                                        }
                                    }
                                }
                            }
                            is AccountsUiState.Error -> {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.Warning, contentDescription = null, tint = RoseUnpaid, modifier = Modifier.size(36.dp))
                                            Text(state.message, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            is AccountsUiState.Success -> {
                                val accounts = state.accounts
                                if (accounts.isEmpty()) {
                                    item {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp).border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                                            shape = RoundedCornerShape(24.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(36.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = IndigoAccent, modifier = Modifier.size(52.dp))
                                                Text(
                                                    text = if (searchQuery.isNotBlank()) stringResource(R.string.empty_search_message) else stringResource(R.string.empty_accounts_title),
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
                        scope.launch { snackbarHostState.showSnackbar(if (wasEdit) updatedMsg else savedMsg) }
                    },
                    onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                )
            }
        )
    }

    pendingDeleteAccount?.let { accountToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteAccount = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = RoseUnpaid) },
            title = { Text(stringResource(R.string.delete_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_dialog_message, accountToDelete.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        val idToDelete = accountToDelete.id
                        pendingDeleteAccount = null
                        viewModel.deleteAccount(
                            accountId = idToDelete,
                            onSuccess = { scope.launch { snackbarHostState.showSnackbar(deletedMsg) } },
                            onError = { err -> scope.launch { snackbarHostState.showSnackbar(err) } }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseUnpaid)
                ) {
                    Text(stringResource(R.string.delete_action), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAccount = null }) {
                    Text(stringResource(R.string.cancel_action))
                }
            }
        )
    }
}

@Composable
private fun SearchResultsDedicatedPage(
    allAccounts: List<AccountRecord>,
    initialQuery: String,
    selectedTarget: SearchTargetField,
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
    val q = initialQuery.trim().lowercase()
    val matchedAccounts = remember(allAccounts, q, selectedTarget) {
        if (q.isEmpty()) {
            allAccounts
        } else {
            allAccounts.filter { r ->
                when (selectedTarget) {
                    SearchTargetField.ALL ->
                        r.name.lowercase().contains(q) ||
                            r.accountName.lowercase().contains(q) ||
                            r.streetName.lowercase().contains(q) ||
                            r.houseNumber.lowercase().contains(q) ||
                            r.flowAmount.lowercase().contains(q) ||
                            r.notes.lowercase().contains(q)
                    SearchTargetField.NAME -> r.name.lowercase().contains(q)
                    SearchTargetField.LOCATION -> r.streetName.lowercase().contains(q) || r.houseNumber.lowercase().contains(q)
                    SearchTargetField.ACCOUNT -> r.accountName.lowercase().contains(q)
                }
            }
        }
    }

    val availableStreets = remember(allAccounts) {
        allAccounts.map { it.streetName.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp).border(1.5.dp, borderColor, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IndigoAccent.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onBack() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = IndigoAccent)
                                    Text("باش بەت", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column {
                                Text("مەخسۇس ئىزدەش بېتى", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Text("ئىسىم، ئورۇن ياكى ھېسابات بويىچە ئايرىم بەتتە تەكشۈرۈش", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        OutlinedTextField(
                            value = initialQuery,
                            onValueChange = onQueryChange,
                            placeholder = { Text("ئىسىم، كوچا/ئۆي نومۇرى ياكى ھېسابات نومۇرىنى يېزىڭ...") },
                            leadingIcon = { Icon(selectedTarget.icon, contentDescription = null, tint = TurquoiseAccent) },
                            trailingIcon = {
                                if (initialQuery.isNotEmpty()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SearchTargetField.entries.forEach { target ->
                                FilterChip(
                                    selected = selectedTarget == target,
                                    onClick = { onTargetChange(target) },
                                    leadingIcon = { Icon(target.icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    label = { Text(target.title) }
                                )
                            }
                        }

                        if (availableStreets.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ئورۇنلار:", style = MaterialTheme.typography.labelSmall)
                                availableStreets.forEach { street ->
                                    FilterChip(
                                        selected = initialQuery.equals(street, ignoreCase = true),
                                        onClick = {
                                            onTargetChange(SearchTargetField.LOCATION)
                                            onQueryChange(street)
                                        },
                                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp)) },
                                        label = { Text(street) }
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatBadgePill(Icons.Filled.Search, "تېپىلغىنى: ${matchedAccounts.size} دانە", IndigoAccent, Color.White)
                            StatBadgePill(Icons.Filled.CheckCircle, "تولۇق تۆلىگەن: ${matchedAccounts.count { it.paidMonthsCount() == 12 }}", EmeraldPaid.copy(alpha = 0.2f), EmeraldPaid)
                        }
                    }
                }
            }

            if (matchedAccounts.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp).border(1.dp, borderColor, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(48.dp))
                            Text("ماس كېلىدىغان ھېسابات تېپىلمىدى", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(matchedAccounts, key = { it.id }) { account ->
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
