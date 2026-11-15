package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AccountRecordEntity
import com.example.data.AccountRepository
import com.example.data.HesabatDatabase
import com.example.data.ThemePreferences
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecordFilterMode {
  ALL,
  FULLY_PAID,
  HAS_UNPAID,
  CURRENT_MONTH_UNPAID,
}

data class AccountEditorState(
    val isVisible: Boolean = false,
    val editingLocalId: Long = 0L,
    val editingExternalId: String = "",
    val editingCreatedAt: Long = 0L,
    val name: String = "",
    val accountName: String = "",
    val password: String = "",
    val flowAmount: String = "",
    val openDate: String = "",
    val streetName: String = "",
    val houseNumber: String = "",
    val notes: String = "",
    val months: List<Boolean> = List(12) { false },
    val errorMessage: String? = null,
) {
  val isEditMode: Boolean
    get() = editingLocalId != 0L
}

data class HesabatUiState(
    val isDarkTheme: Boolean = true,
    val searchQuery: String = "",
    val filterMode: RecordFilterMode = RecordFilterMode.ALL,
    val selectedStreet: String? = null,
    val allRecordsCount: Int = 0,
    val totalPaidMonthsCount: Int = 0,
    val totalUnpaidMonthsCount: Int = 0,
    val fullyPaidAccountsCount: Int = 0,
    val currentMonthIndex: Int = 0,
    val availableStreets: List<String> = emptyList(),
    val streetBreakdown: List<Pair<String, Int>> = emptyList(),
    val filteredRecords: List<AccountRecordEntity> = emptyList(),
    val editorState: AccountEditorState = AccountEditorState(),
    val pendingDeleteRecord: AccountRecordEntity? = null,
    val isBackupSheetVisible: Boolean = false,
    val snackbarMessage: String? = null,
)

class HesabatViewModel(application: Application) : AndroidViewModel(application) {
  private val database = HesabatDatabase.getInstance(application)
  private val repository = AccountRepository(database.accountRecordDao())
  private val themePreferences = ThemePreferences(application)

  private val searchQueryFlow = MutableStateFlow("")
  private val filterModeFlow = MutableStateFlow(RecordFilterMode.ALL)
  private val selectedStreetFlow = MutableStateFlow<String?>(null)
  private val editorStateFlow = MutableStateFlow(AccountEditorState())
  private val pendingDeleteFlow = MutableStateFlow<AccountRecordEntity?>(null)
  private val backupSheetVisibleFlow = MutableStateFlow(false)
  private val snackbarMessageFlow = MutableStateFlow<String?>(null)

  private val currentMonthIndex: Int
    get() = Calendar.getInstance().get(Calendar.MONTH).coerceIn(0, 11)

  private val filterParamsFlow =
      combine(searchQueryFlow, filterModeFlow, selectedStreetFlow) { query, mode, street ->
        Triple(query, mode, street)
      }

  private val modalParamsFlow =
      combine(editorStateFlow, pendingDeleteFlow, backupSheetVisibleFlow, snackbarMessageFlow) {
          editor,
          pendingDelete,
          backupVisible,
          snackbar ->
        Quad(editor, pendingDelete, backupVisible, snackbar)
      }

  val uiState: StateFlow<HesabatUiState> =
      combine(
              repository.allRecords,
              themePreferences.isDarkTheme,
              filterParamsFlow,
              modalParamsFlow,
          ) { records, isDark, (query, filterMode, selectedStreet), modal ->
            val curMonth = currentMonthIndex
            val trimmedQuery = query.trim().lowercase()

            val streets =
                records
                    .map { it.streetName.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()

            val streetStats =
                records
                    .filter { it.streetName.isNotBlank() }
                    .groupBy { it.streetName.trim() }
                    .map { (street, items) -> street to items.size }
                    .sortedByDescending { it.second }

            val activeStreet = if (selectedStreet in streets) selectedStreet else null

            val filtered =
                records.filter { record ->
                  val matchesQuery =
                      trimmedQuery.isEmpty() ||
                          record.name.lowercase().contains(trimmedQuery) ||
                          record.accountName.lowercase().contains(trimmedQuery) ||
                          record.streetName.lowercase().contains(trimmedQuery) ||
                          record.houseNumber.lowercase().contains(trimmedQuery) ||
                          record.flowAmount.lowercase().contains(trimmedQuery) ||
                          record.notes.lowercase().contains(trimmedQuery)

                  val paidCount = record.paidMonthsCount()
                  val matchesFilter =
                      when (filterMode) {
                        RecordFilterMode.ALL -> true
                        RecordFilterMode.FULLY_PAID -> paidCount == 12
                        RecordFilterMode.HAS_UNPAID -> paidCount < 12
                        RecordFilterMode.CURRENT_MONTH_UNPAID -> !record.isMonthPaid(curMonth)
                      }

                  val matchesStreet =
                      activeStreet == null || record.streetName.trim() == activeStreet

                  matchesQuery && matchesFilter && matchesStreet
                }

            val totalPaidMonths = records.sumOf { it.paidMonthsCount() }
            val totalPossibleMonths = records.size * 12
            val totalUnpaidMonths = (totalPossibleMonths - totalPaidMonths).coerceAtLeast(0)
            val fullyPaidCount = records.count { it.paidMonthsCount() == 12 }

            HesabatUiState(
                isDarkTheme = isDark,
                searchQuery = query,
                filterMode = filterMode,
                selectedStreet = activeStreet,
                allRecordsCount = records.size,
                totalPaidMonthsCount = totalPaidMonths,
                totalUnpaidMonthsCount = totalUnpaidMonths,
                fullyPaidAccountsCount = fullyPaidCount,
                currentMonthIndex = curMonth,
                availableStreets = streets,
                streetBreakdown = streetStats,
                filteredRecords = filtered,
                editorState = modal.first,
                pendingDeleteRecord = modal.second,
                isBackupSheetVisible = modal.third,
                snackbarMessage = modal.fourth,
            )
          }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5_000),
              initialValue = HesabatUiState(),
          )

  fun updateSearchQuery(query: String) {
    searchQueryFlow.value = query
  }

  fun clearSearch() {
    searchQueryFlow.value = ""
  }

  fun setFilterMode(mode: RecordFilterMode) {
    filterModeFlow.value = mode
  }

  fun toggleStreetFilter(street: String?) {
    selectedStreetFlow.value = if (selectedStreetFlow.value == street) null else street
  }

  fun toggleTheme() {
    val next = !uiState.value.isDarkTheme
    viewModelScope.launch { themePreferences.setDarkTheme(next) }
  }

  fun toggleMonthStatus(record: AccountRecordEntity, monthIndex: Int) {
    viewModelScope.launch { repository.toggleMonth(record, monthIndex) }
  }

  fun openNewAccountEditor() {
    editorStateFlow.value = AccountEditorState(isVisible = true)
  }

  fun openEditAccountEditor(record: AccountRecordEntity) {
    editorStateFlow.value =
        AccountEditorState(
            isVisible = true,
            editingLocalId = record.localId,
            editingExternalId = record.externalId,
            editingCreatedAt = record.createdAt,
            name = record.name,
            accountName = record.accountName,
            password = record.password,
            flowAmount = record.flowAmount,
            openDate = record.openDate,
            streetName = record.streetName,
            houseNumber = record.houseNumber,
            notes = record.notes,
            months = record.toMonthsList(),
            errorMessage = null,
        )
  }

  fun dismissEditor() {
    editorStateFlow.value = AccountEditorState(isVisible = false)
  }

  fun updateEditorFields(transform: (AccountEditorState) -> AccountEditorState) {
    editorStateFlow.update { current -> transform(current).copy(errorMessage = null) }
  }

  fun toggleEditorMonth(index: Int) {
    editorStateFlow.update { current ->
      val updated = current.months.toMutableList()
      if (index in 0..11) {
        updated[index] = !updated[index]
      }
      current.copy(months = updated)
    }
  }

  fun setAllEditorMonths(paid: Boolean) {
    editorStateFlow.update { current -> current.copy(months = List(12) { paid }) }
  }

  fun saveEditorRecord() {
    val state = editorStateFlow.value
    val trimmedName = state.name.trim()
    val trimmedAccount = state.accountName.trim()
    if (trimmedName.isEmpty() || trimmedAccount.isEmpty()) {
      editorStateFlow.update {
        it.copy(
            errorMessage = getApplication<Application>().getString(R.string.validation_required_fields)
        )
      }
      return
    }

    val now = System.currentTimeMillis()
    val entity =
        AccountRecordEntity(
            localId = state.editingLocalId,
            externalId =
                state.editingExternalId.ifBlank { now.toString() },
            name = trimmedName,
            accountName = trimmedAccount,
            password = state.password,
            flowAmount = state.flowAmount.trim(),
            openDate = state.openDate.trim(),
            streetName = state.streetName.trim(),
            houseNumber = state.houseNumber.trim(),
            notes = state.notes.trim(),
            monthsMask = AccountRecordEntity.monthsMaskFromList(state.months),
            createdAt = if (state.isEditMode) state.editingCreatedAt else now,
            updatedAt = now,
        )

    viewModelScope.launch {
      repository.saveRecord(entity)
      val msgRes = if (state.isEditMode) R.string.updated_success else R.string.saved_success
      editorStateFlow.value = AccountEditorState(isVisible = false)
      snackbarMessageFlow.value = getApplication<Application>().getString(msgRes)
    }
  }

  fun requestDelete(record: AccountRecordEntity) {
    pendingDeleteFlow.value = record
  }

  fun dismissDelete() {
    pendingDeleteFlow.value = null
  }

  fun confirmDelete() {
    val target = pendingDeleteFlow.value ?: return
    pendingDeleteFlow.value = null
    viewModelScope.launch {
      repository.deleteRecord(target.localId)
      snackbarMessageFlow.value = getApplication<Application>().getString(R.string.deleted_success)
    }
  }

  fun openBackupSheet() {
    backupSheetVisibleFlow.value = true
  }

  fun dismissBackupSheet() {
    backupSheetVisibleFlow.value = false
  }

  suspend fun generateExportJson(): String = repository.exportAllToJson()

  fun importBackupJson(json: String) {
    viewModelScope.launch {
      try {
        val count = repository.importFromJson(json)
        snackbarMessageFlow.value =
            getApplication<Application>().getString(R.string.backup_import_success, count)
        backupSheetVisibleFlow.value = false
      } catch (_: Exception) {
        snackbarMessageFlow.value =
            getApplication<Application>().getString(R.string.backup_invalid_format)
      }
    }
  }

  fun showMessage(message: String) {
    snackbarMessageFlow.value = message
  }

  fun consumeSnackbarMessage() {
    snackbarMessageFlow.value = null
  }

  private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
