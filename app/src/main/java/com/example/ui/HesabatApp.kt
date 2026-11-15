package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.AccountEditorSheet
import com.example.ui.components.AccountRecordCard
import com.example.ui.components.BackupAndStatsSheet
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.HeaderDashboard
import com.example.ui.components.ScenicBackground
import com.example.ui.components.SearchAndFilterBar
import com.example.ui.theme.HesabatTheme
import com.example.ui.theme.LocalHesabatColors
import com.example.viewmodel.HesabatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HesabatApp(viewModel: HesabatViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // SAF JSON Export Launcher
  val exportLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.CreateDocument("application/json")
      ) { uri ->
        if (uri != null) {
          scope.launch {
            try {
              val json = viewModel.generateExportJson()
              withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                  out.write(json.toByteArray(Charsets.UTF_8))
                }
              }
              viewModel.showMessage(context.getString(R.string.backup_export_success))
            } catch (_: Exception) {
              viewModel.showMessage(context.getString(R.string.backup_error))
            }
          }
        }
      }

  // SAF JSON Import Launcher
  val importLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
          scope.launch {
            try {
              val content =
                  withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                      it.readText()
                    }
                  }
              if (content != null) {
                viewModel.importBackupJson(content)
              } else {
                viewModel.showMessage(context.getString(R.string.backup_error))
              }
            } catch (_: Exception) {
              viewModel.showMessage(context.getString(R.string.backup_error))
            }
          }
        }
      }

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.consumeSnackbarMessage()
    }
  }

  // Back button closes open sheets first
  BackHandler(enabled = uiState.editorState.isVisible || uiState.isBackupSheetVisible) {
    when {
      uiState.editorState.isVisible -> viewModel.dismissEditor()
      uiState.isBackupSheetVisible -> viewModel.dismissBackupSheet()
    }
  }

  HesabatTheme(darkTheme = uiState.isDarkTheme) {
    Box(modifier = Modifier.fillMaxSize()) {
      ScenicBackground()

      Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = Color.Transparent,
          contentWindowInsets = WindowInsets.safeDrawing,
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewAccountEditor() },
                icon = {
                  Icon(
                      imageVector = Icons.Filled.Add,
                      contentDescription = stringResource(R.string.cd_add_account),
                  )
                },
                text = { Text(text = stringResource(R.string.action_add_new)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_account"),
            )
          },
      ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
          LazyColumn(
              modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            item(key = "header_dashboard") {
              HeaderDashboard(
                  totalCount = uiState.allRecordsCount,
                  paidMonthsCount = uiState.totalPaidMonthsCount,
                  unpaidMonthsCount = uiState.totalUnpaidMonthsCount,
                  fullyPaidAccountsCount = uiState.fullyPaidAccountsCount,
                  isDarkTheme = uiState.isDarkTheme,
                  onToggleTheme = { viewModel.toggleTheme() },
                  onOpenBackupSheet = { viewModel.openBackupSheet() },
              )
            }

            item(key = "search_and_filter") {
              SearchAndFilterBar(
                  searchQuery = uiState.searchQuery,
                  filterMode = uiState.filterMode,
                  selectedStreet = uiState.selectedStreet,
                  availableStreets = uiState.availableStreets,
                  resultCount = uiState.filteredRecords.size,
                  onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                  onClearSearch = { viewModel.clearSearch() },
                  onFilterModeChange = { viewModel.setFilterMode(it) },
                  onToggleStreet = { viewModel.toggleStreetFilter(it) },
              )
            }

            if (uiState.filteredRecords.isEmpty()) {
              item(key = "empty_state") {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                  EmptyRecordsCard(
                      isSearchingOrFiltering =
                          uiState.searchQuery.isNotBlank() || uiState.allRecordsCount > 0
                  )
                }
              }
            } else {
              items(
                  items = uiState.filteredRecords,
                  key = { it.localId },
              ) { record ->
                AccountRecordCard(
                    record = record,
                    currentMonthIndex = uiState.currentMonthIndex,
                    onToggleMonth = { monthIndex ->
                      viewModel.toggleMonthStatus(record, monthIndex)
                    },
                    onEdit = { viewModel.openEditAccountEditor(record) },
                    onDelete = { viewModel.requestDelete(record) },
                    onCopyValue = { value ->
                      copyToClipboard(context, value)
                      viewModel.showMessage(context.getString(R.string.copied_toast, value))
                    },
                )
              }
            }
          }
        }
      }

      if (uiState.editorState.isVisible) {
        AccountEditorSheet(
            state = uiState.editorState,
            onFieldsChange = { viewModel.updateEditorFields(it) },
            onToggleMonth = { viewModel.toggleEditorMonth(it) },
            onSetAllMonths = { viewModel.setAllEditorMonths(it) },
            onSave = { viewModel.saveEditorRecord() },
            onDismiss = { viewModel.dismissEditor() },
        )
      }

      if (uiState.isBackupSheetVisible) {
        BackupAndStatsSheet(
            totalAccounts = uiState.allRecordsCount,
            paidMonthsCount = uiState.totalPaidMonthsCount,
            unpaidMonthsCount = uiState.totalUnpaidMonthsCount,
            streetBreakdown = uiState.streetBreakdown,
            onExportJson = {
              val dateTag = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
              exportLauncher.launch("hesabat_backup_$dateTag.json")
            },
            onImportJson = {
              importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onDismiss = { viewModel.dismissBackupSheet() },
        )
      }

      uiState.pendingDeleteRecord?.let { target ->
        DeleteConfirmationDialog(
            record = target,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDelete() },
        )
      }
    }
  }
}

@Composable
private fun EmptyRecordsCard(isSearchingOrFiltering: Boolean) {
  val glass = LocalHesabatColors.current
  Surface(
      modifier = Modifier.fillMaxWidth().testTag("empty_records_card"),
      shape = MaterialTheme.shapes.large,
      color = glass.glassSurface,
      border = BorderStroke(1.dp, glass.glassBorder),
      shadowElevation = 4.dp,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
          imageVector = Icons.Filled.FolderOpen,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
          modifier = Modifier.size(52.dp),
      )
      Text(
          text =
              stringResource(
                  if (isSearchingOrFiltering) R.string.no_matching_records
                  else R.string.empty_state_title
              ),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
      )
      if (!isSearchingOrFiltering) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.empty_state_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
      }
    }
  }
}

private fun copyToClipboard(context: Context, value: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText("hesabat", value))
}
