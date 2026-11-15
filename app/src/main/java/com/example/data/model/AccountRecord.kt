package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class AccountRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val accountName: String = "",
    val password: String = "",
    val flowAmount: String = "",
    val openDate: String = "",
    val streetName: String = "",
    val houseNumber: String = "",
    val notes: String = "",
    val months: List<Boolean> = List(12) { false },
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun normalizedMonths(): List<Boolean> {
        if (months.size == 12) return months
        return List(12) { index -> months.getOrElse(index) { false } }
    }

    fun paidMonthsCount(): Int = normalizedMonths().count { it }
}
