package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AccountRecord
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAccount_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = AccountRepository(firestore)

        val record = AccountRecord(
            name = "ئەخمەت",
            accountName = "NET-1001",
            password = "pass123",
            flowAmount = "100M / 1000GB",
            openDate = "2026-01-15",
            streetName = "ئازادلىق كوچىسى",
            houseNumber = "12-نومۇر",
            notes = "ئاكتىپ",
            months = List(12) { it == 0 }
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createAccount(record) }
        assertTrue(createResult.isSuccess)
        val accountId = createResult.getOrThrow()
        assertTrue(accountId.isNotEmpty())
    }

    @Test
    fun observeAccounts_authenticatedOwner_emitsRealtimeUpdatesAndTogglesMonth() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = AccountRepository(firestore)

        val accountId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createAccount(
                AccountRecord(
                    name = "مەمەت",
                    accountName = "NET-2002",
                    months = List(12) { false }
                )
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAccounts(uid).first { list -> list.any { it.id == accountId } }
        }
        val created = emitted.first { it.id == accountId }
        assertEquals("مەمەت", created.name)
        assertEquals(0, created.paidMonthsCount())

        val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleMonthStatus(accountId, created.months, 2)
        }
        assertTrue(toggleResult.isSuccess)

        val updatedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAccounts(uid).first { list ->
                list.firstOrNull { it.id == accountId }?.normalizedMonths()?.get(2) == true
            }
        }
        assertEquals(1, updatedList.first { it.id == accountId }.paidMonthsCount())
    }

    @Test
    fun observeAccounts_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = AccountRepository(firestore)
        val accountId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createAccount(
                AccountRecord(name = "ئالىم", accountName = "NET-3003")
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = AccountRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeAccounts(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException, got $caught", firestoreException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            firestoreException?.code
        )
        assertTrue(accountId.isNotEmpty())
    }

    @Test
    fun observeAccounts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = AccountRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeAccounts(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException, got $caught", firestoreException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            firestoreException?.code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice_hesabat@test.com"
        const val BOB_EMAIL = "bob_hesabat@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
