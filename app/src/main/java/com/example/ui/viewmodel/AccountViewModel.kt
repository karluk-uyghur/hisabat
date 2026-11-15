package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountRecord
import com.example.data.repository.AccountRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Success(val accounts: List<AccountRecord>) : AccountsUiState
    data class Error(val message: String) : AccountsUiState
}

enum class AccountFilter {
    ALL,
    HAS_UNPAID,
    FULLY_PAID
}

enum class AccountSort {
    NEWEST,
    NAME,
    PAID_PROGRESS
}

class AccountViewModel(
    private val repository: AccountRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(AccountFilter.ALL)
    val selectedFilter: StateFlow<AccountFilter> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(AccountSort.NEWEST)
    val selectedSort: StateFlow<AccountSort> = _selectedSort.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val rawAccountsState: StateFlow<AccountsUiState> = repository.observeAccounts(currentUserId)
        .map<List<AccountRecord>, AccountsUiState> { AccountsUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing accounts", rootError)
            emit(AccountsUiState.Error(rootError.message ?: "ھېساباتلارنى يۈكلەشتە خاتالىق كۆرۈلدى"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AccountsUiState.Loading
        )

    val filteredAccountsState: StateFlow<AccountsUiState> = combine(
        rawAccountsState,
        _searchQuery,
        _selectedFilter,
        _selectedSort
    ) { state, query, filter, sort ->
        when (state) {
            is AccountsUiState.Loading -> AccountsUiState.Loading
            is AccountsUiState.Error -> state
            is AccountsUiState.Success -> {
                val q = query.trim().lowercase()
                val searched = if (q.isEmpty()) {
                    state.accounts
                } else {
                    state.accounts.filter { r ->
                        r.name.lowercase().contains(q) ||
                            r.accountName.lowercase().contains(q) ||
                            r.streetName.lowercase().contains(q) ||
                            r.houseNumber.lowercase().contains(q) ||
                            r.notes.lowercase().contains(q)
                    }
                }
                val filtered = when (filter) {
                    AccountFilter.ALL -> searched
                    AccountFilter.HAS_UNPAID -> searched.filter { it.paidMonthsCount() < 12 }
                    AccountFilter.FULLY_PAID -> searched.filter { it.paidMonthsCount() == 12 }
                }
                val sorted = when (sort) {
                    AccountSort.NEWEST -> filtered.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    AccountSort.NAME -> filtered.sortedBy { it.name.lowercase() }
                    AccountSort.PAID_PROGRESS -> filtered.sortedByDescending { it.paidMonthsCount() }
                }
                AccountsUiState.Success(sorted)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AccountsUiState.Loading
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: AccountFilter) {
        _selectedFilter.value = filter
    }

    fun updateSort(sort: AccountSort) {
        _selectedSort.value = sort
    }

    fun saveAccount(
        editingId: String?,
        name: String,
        accountName: String,
        password: String,
        flowAmount: String,
        openDate: String,
        streetName: String,
        houseNumber: String,
        notes: String,
        months: List<Boolean>,
        onSuccess: (isEdit: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val record = AccountRecord(
                id = editingId.orEmpty(),
                userId = currentUserId,
                name = name.trim(),
                accountName = accountName.trim(),
                password = password,
                flowAmount = flowAmount.trim(),
                openDate = openDate.trim(),
                streetName = streetName.trim(),
                houseNumber = houseNumber.trim(),
                notes = notes.trim(),
                months = List(12) { idx -> months.getOrElse(idx) { false } }
            )
            val result = if (editingId.isNullOrBlank()) {
                repository.createAccount(record).map { Unit }
            } else {
                repository.updateAccount(record)
            }
            _isSaving.value = false
            result.onSuccess {
                onSuccess(!editingId.isNullOrBlank())
            }.onFailure { err ->
                onError(err.localizedMessage ?: "ساقلاشتا خاتالىق كۆرۈلدى")
            }
        }
    }

    fun toggleMonth(account: AccountRecord, monthIndex: Int, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.toggleMonthStatus(account.id, account.normalizedMonths(), monthIndex)
                .onFailure { err ->
                    onError(err.localizedMessage ?: "ئايلىق ھالەتنى ئۆزگەرتىشتە خاتالىق كۆرۈلدى")
                }
        }
    }

    fun deleteAccount(
        accountId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
                .onSuccess { onSuccess() }
                .onFailure { err ->
                    onError(err.localizedMessage ?: "ئۆچۈرۈشتە خاتالىق كۆرۈلدى")
                }
        }
    }

    fun buildBackupJson(): String {
        val accounts = (rawAccountsState.value as? AccountsUiState.Success)?.accounts ?: emptyList()
        val array = JSONArray()
        accounts.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("accountName", item.accountName)
                put("password", item.password)
                put("flowAmount", item.flowAmount)
                put("openDate", item.openDate)
                put("streetName", item.streetName)
                put("houseNumber", item.houseNumber)
                put("notes", item.notes)
                put("months", JSONArray(item.normalizedMonths()))
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    fun importBackupJson(
        jsonString: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray(jsonString)
                val parsed = mutableListOf<AccountRecord>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i) ?: continue
                    val name = obj.optString("name", "").trim()
                    val accountName = obj.optString("accountName", "").trim()
                    if (name.isEmpty() || accountName.isEmpty()) continue
                    val monthsArray = obj.optJSONArray("months")
                    val months = List(12) { mIdx ->
                        monthsArray?.optBoolean(mIdx, false) ?: false
                    }
                    parsed.add(
                        AccountRecord(
                            userId = currentUserId,
                            name = name,
                            accountName = accountName,
                            password = obj.optString("password", ""),
                            flowAmount = obj.optString("flowAmount", ""),
                            openDate = obj.optString("openDate", ""),
                            streetName = obj.optString("streetName", ""),
                            houseNumber = obj.optString("houseNumber", ""),
                            notes = obj.optString("notes", ""),
                            months = months
                        )
                    )
                }
                if (parsed.isEmpty()) {
                    onError("يۈكلەيدىغان ئىناۋەتلىك ھېسابات تېپىلمىدى.")
                    return@launch
                }
                repository.importAccounts(parsed)
                    .onSuccess { count -> onSuccess(count) }
                    .onFailure { err ->
                        onError(err.localizedMessage ?: "زاپاس يۈكلەشتە خاتالىق كۆرۈلدى")
                    }
            } catch (e: Exception) {
                onError("JSON ھۆججىتىنى ئوقۇشتا خاتالىق كۆرۈلدى.")
            }
        }
    }

    private companion object {
        const val TAG = "AccountVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
