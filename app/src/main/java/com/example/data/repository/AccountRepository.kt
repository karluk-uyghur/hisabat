package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.AccountRecord
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AccountRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("ھېسابات ئۇچۇرلىرىنى زىيارەت قىلىش ئۈچۈن ئالدى بىلەن كىرىشىڭىز كېرەك.")
    }

    private fun accountsCollection(uid: String) =
        db.collection("users").document(uid).collection("accounts")

    fun observeAccounts(userId: String): Flow<List<AccountRecord>> = flow {
        val path = "users/$userId/accounts"
        emitAll(
            accountsCollection(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        AccountRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getAccountById(accountId: String): Result<AccountRecord?> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = accountsCollection(uid).document(accountId)
        return try {
            val snapshot = docRef.get().await()
            val record = snapshot.toObject(
                AccountRecord::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(record)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createAccount(record: AccountRecord): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = accountsCollection(uid).document()
        val payload = mapOf(
            "userId" to uid,
            "name" to record.name.trim(),
            "accountName" to record.accountName.trim(),
            "password" to record.password,
            "flowAmount" to record.flowAmount.trim(),
            "openDate" to record.openDate.trim(),
            "streetName" to record.streetName.trim(),
            "houseNumber" to record.houseNumber.trim(),
            "notes" to record.notes.trim(),
            "months" to record.normalizedMonths(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateAccount(record: AccountRecord): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        require(record.id.isNotBlank()) { "Account ID must not be blank for update" }
        val docRef = accountsCollection(uid).document(record.id)
        val updates = mapOf(
            "name" to record.name.trim(),
            "accountName" to record.accountName.trim(),
            "password" to record.password,
            "flowAmount" to record.flowAmount.trim(),
            "openDate" to record.openDate.trim(),
            "streetName" to record.streetName.trim(),
            "houseNumber" to record.houseNumber.trim(),
            "notes" to record.notes.trim(),
            "months" to record.normalizedMonths(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleMonthStatus(
        accountId: String,
        currentMonths: List<Boolean>,
        monthIndex: Int
    ): Result<Unit> {
        if (monthIndex !in 0..11) {
            return Result.failure(IllegalArgumentException("Invalid month index: $monthIndex"))
        }
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val updatedMonths = MutableList(12) { index -> currentMonths.getOrElse(index) { false } }
        updatedMonths[monthIndex] = !updatedMonths[monthIndex]
        val docRef = accountsCollection(uid).document(accountId)
        val updates = mapOf(
            "months" to updatedMonths,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(accountId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = accountsCollection(uid).document(accountId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun importAccounts(records: List<AccountRecord>): Result<Int> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val validRecords = records.filter { it.name.isNotBlank() && it.accountName.isNotBlank() }
        if (validRecords.isEmpty()) return Result.success(0)

        return try {
            var count = 0
            validRecords.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { record ->
                    val docRef = accountsCollection(uid).document()
                    val payload = mapOf(
                        "userId" to uid,
                        "name" to record.name.trim().take(120),
                        "accountName" to record.accountName.trim().take(120),
                        "password" to record.password.take(120),
                        "flowAmount" to record.flowAmount.trim().take(80),
                        "openDate" to record.openDate.trim().take(30),
                        "streetName" to record.streetName.trim().take(120),
                        "houseNumber" to record.houseNumber.trim().take(60),
                        "notes" to record.notes.trim().take(500),
                        "months" to record.normalizedMonths(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, payload)
                    count++
                }
                batch.commit().await()
            }
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/accounts")
            Result.failure(e)
        }
    }
}
