package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AccountRecord
import com.example.ui.auth.signOut
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

@Composable
fun DashboardScreen(
    viewModel: AccountViewModel,
    isDarkGlass: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val rawState by viewModel.rawAccountsState.collectAsStateWithLifecycle()
    val filteredState by viewModel.filteredAccountsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var showDataMenu by remember { mutableStateOf(false) }
    var showFormSheet by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountRecord?>(null) }
    var pendingDeleteAccount by remember { mutableStateOf<AccountRecord?>(null) }

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
            } catch (e: Exception) {
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
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar(importFailedMsg) }
            }
        }
    }

    val allAccounts = (rawState as? AccountsUiState.Success)?.accounts ?: emptyList()
    val totalCount = allAccounts.size
    val fullyPaidCount = allAccounts.count { it.paidMonthsCount() == 12 }
    val totalPaidMonths = allAccounts.sumOf { it.paidMonthsCount() }

    val borderColor = if (isDarkGlass) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.75f)
    val scrimColors = if (isDarkGlass) {
        listOf(Color(0x66060E1F), Color(0x990A162E))
    } else {
        listOf(Color(0x22FFFFFF), Color(0x44E2E8F0))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // User's Landscape Photo Background
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
                    // Header Glass Card
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
                                            modifier = Modifier
                                                .size(48.dp)
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
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Column {
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

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                                contentDescription = stringResource(R.string.backup_menu_cd)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                signOut(credentialManager, scope)
                                            },
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
                                        Text(
                                            text = "${stringResource(R.string.paid_months_summary_label)}: $totalPaidMonths",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                        )
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

                    // Search & Filter Glass Card
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
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = stringResource(R.string.clear_search_cd)
                                                )
                                            }
                                        }
                                    },
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

                                // Filter & Sort Chips
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
                                                    onCopyText = { label, value ->
                                                        clipboardManager.setText(AnnotatedString(value))
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                context.getString(R.string.copied_to_clipboard, label)
                                                            )
                                                        }
                                                    },
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
                                            onCopyText = { label, value ->
                                                clipboardManager.setText(AnnotatedString(value))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        context.getString(R.string.copied_to_clipboard, label)
                                                    )
                                                }
                                            },
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
