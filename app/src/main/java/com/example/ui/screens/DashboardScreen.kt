package com.example.data.repository

import android.content.Context
import com.example.data.model.AccountRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AccountRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore? = null
) {
    private val mutex = Mutex()
    private val prefs = context.getSharedPreferences(PREFS_VAULT_NAME, Context.MODE_PRIVATE)
    private val dbFile: File
        get() = File(context.filesDir, DB_FILE_NAME)

    private val recordsFlow = MutableStateFlow<List<AccountRecord>>(emptyList())

    init {
        recordsFlow.value = readAllFromDisk()
    }

    fun observeAccounts(userId: String): Flow<List<AccountRecord>> {
        return recordsFlow.map { list ->
            val cleanUser = userId.trim()
            list.filter { record ->
                record.userId.isBlank() ||
                    record.userId.equals(cleanUser, ignoreCase = true) ||
                    record.userId == "shared_local"
            }
        }
    }

    suspend fun createAccount(record: AccountRecord): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                val nowSeconds = System.currentTimeMillis() / 1000
                val generatedId = if (record.id.isBlank()) UUID.randomUUID().toString() else record.id
                val newRecord = record.copy(
                    id = generatedId,
                    months = record.normalizedMonths(),
                    createdAt = record.createdAt ?: Timestamp(nowSeconds, 0),
                    updatedAt = Timestamp(nowSeconds, 0)
                )
                val current = recordsFlow.value.toMutableList()
                current.add(0, newRecord)
                persistAll(current)
                recordsFlow.value = current
                syncToCloudSafely(newRecord)
                generatedId
            }
        }
    }

    suspend fun updateAccount(record: AccountRecord): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                val nowSeconds = System.currentTimeMillis() / 1000
                val current = recordsFlow.value.toMutableList()
                val idx = current.indexOfFirst { it.id == record.id }
                val updated = record.copy(
                    months = record.normalizedMonths(),
                    createdAt = if (idx >= 0) current[idx].createdAt else (record.createdAt ?: Timestamp(nowSeconds, 0)),
                    updatedAt = Timestamp(nowSeconds, 0)
                )
                if (idx >= 0) {
                    current[idx] = updated
                } else {
                    current.add(0, updated)
                }
                persistAll(current)
                recordsFlow.value = current
                syncToCloudSafely(updated)
            }
        }
    }

    suspend fun toggleMonthStatus(
        accountId: String,
        currentMonths: List<Boolean>,
        monthIndex: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                if (monthIndex !in 0..11) return@withLock
                val current = recordsFlow.value.toMutableList()
                val idx = current.indexOfFirst { it.id == accountId }
                if (idx >= 0) {
                    val normalized = List(12) { i -> currentMonths.getOrElse(i) { false } }.toMutableList()
                    normalized[monthIndex] = !normalized[monthIndex]
                    val nowSeconds = System.currentTimeMillis() / 1000
                    val updated = current[idx].copy(
                        months = normalized,
                        updatedAt = Timestamp(nowSeconds, 0)
                    )
                    current[idx] = updated
                    persistAll(current)
                    recordsFlow.value = current
                    syncToCloudSafely(updated)
                }
            }
        }
    }

    suspend fun deleteAccount(accountId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                val current = recordsFlow.value.filterNot { it.id == accountId }
                persistAll(current)
                recordsFlow.value = current
                runCatching {
                    firestore?.collection("accounts")?.document(accountId)?.delete()?.await()
                }
            }
        }
    }

    suspend fun importAccounts(records: List<AccountRecord>): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                val nowSeconds = System.currentTimeMillis() / 1000
                val current = recordsFlow.value.toMutableList()
                var importedCount = 0
                records.forEach { incoming ->
                    val newId = if (incoming.id.isBlank()) UUID.randomUUID().toString() else incoming.id
                    val prepared = incoming.copy(
                        id = newId,
                        months = incoming.normalizedMonths(),
                        createdAt = incoming.createdAt ?: Timestamp(nowSeconds, 0),
                        updatedAt = Timestamp(nowSeconds, 0)
                    )
                    val existingIdx = current.indexOfFirst {
                        it.id == prepared.id ||
                            (it.accountName.equals(prepared.accountName, ignoreCase = true) &&
                                it.name.equals(prepared.name, ignoreCase = true))
                    }
                    if (existingIdx >= 0) {
                        current[existingIdx] = prepared.copy(id = current[existingIdx].id)
                    } else {
                        current.add(0, prepared)
                    }
                    importedCount++
                }
                persistAll(current)
                recordsFlow.value = current
                importedCount
            }
        }
    }

    private suspend fun syncToCloudSafely(record: AccountRecord) {
        runCatching {
            firestore?.collection("accounts")?.document(record.id)?.set(record)?.await()
        }
    }

    private fun readAllFromDisk(): List<AccountRecord> {
        val fileText = runCatching {
            if (dbFile.exists()) dbFile.readText(Charsets.UTF_8) else ""
        }.getOrDefault("")

        val backupText = prefs.getString(KEY_VAULT_JSON, "").orEmpty()
        val jsonSource = if (fileText.isNotBlank()) fileText else backupText
        if (jsonSource.isBlank()) return emptyList()

        return try {
            val array = JSONArray(jsonSource)
            val result = mutableListOf<AccountRecord>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val monthsArr = obj.optJSONArray("months")
                val months = List(12) { m -> monthsArr?.optBoolean(m, false) ?: false }
                val createdSec = obj.optLong("createdAtSec", System.currentTimeMillis() / 1000)
                val updatedSec = obj.optLong("updatedAtSec", createdSec)
                result.add(
                    AccountRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        userId = obj.optString("userId", ""),
                        name = obj.optString("name", ""),
                        accountName = obj.optString("accountName", ""),
                        password = obj.optString("password", ""),
                        flowAmount = obj.optString("flowAmount", ""),
                        openDate = obj.optString("openDate", ""),
                        streetName = obj.optString("streetName", ""),
                        houseNumber = obj.optString("houseNumber", ""),
                        notes = obj.optString("notes", ""),
                        months = months,
                        createdAt = Timestamp(createdSec, 0),
                        updatedAt = Timestamp(updatedSec, 0)
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistAll(records: List<AccountRecord>) {
        try {
            val array = JSONArray()
            records.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("userId", item.userId)
                    put("name", item.name)
                    put("accountName", item.accountName)
                    put("password", item.password)
                    put("flowAmount", item.flowAmount)
                    put("openDate", item.openDate)
                    put("streetName", item.streetName)
                    put("houseNumber", item.houseNumber)
                    put("notes", item.notes)
                    put("months", JSONArray(item.normalizedMonths()))
                    put("createdAtSec", item.createdAt?.seconds ?: (System.currentTimeMillis() / 1000))
                    put("updatedAtSec", item.updatedAt?.seconds ?: (System.currentTimeMillis() / 1000))
                }
                array.put(obj)
            }
            val serialized = array.toString()
            prefs.edit().putString(KEY_VAULT_JSON, serialized).commit()
            val tmpFile = File(context.filesDir, "$DB_FILE_NAME.tmp")
            tmpFile.writeText(serialized, Charsets.UTF_8)
            if (dbFile.exists()) dbFile.delete()
            tmpFile.renameTo(dbFile)
        } catch (_: Exception) {
        }
    }

    private companion object {
        const val DB_FILE_NAME = "hesabat_local_vault.json"
        const val PREFS_VAULT_NAME = "hesabat_permanent_vault_backup"
        const val KEY_VAULT_JSON = "vault_records_json_v3"
    }
}
