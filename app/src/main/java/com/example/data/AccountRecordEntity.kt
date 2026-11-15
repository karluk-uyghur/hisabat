package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "account_records")
data class AccountRecordEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val externalId: String,
    val name: String,
    val accountName: String,
    val password: String = "",
    val flowAmount: String = "",
    val openDate: String = "",
    val streetName: String = "",
    val houseNumber: String = "",
    val notes: String = "",
    val monthsMask: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
  fun isMonthPaid(monthIndex: Int): Boolean {
    if (monthIndex !in 0..11) return false
    return (monthsMask and (1 shl monthIndex)) != 0
  }

  fun toMonthsList(): List<Boolean> = List(12) { index -> isMonthPaid(index) }

  fun paidMonthsCount(): Int = (0..11).count { isMonthPaid(it) }

  fun withToggledMonth(monthIndex: Int): AccountRecordEntity {
    if (monthIndex !in 0..11) return this
    return copy(
        monthsMask = monthsMask xor (1 shl monthIndex),
        updatedAt = System.currentTimeMillis(),
    )
  }

  companion object {
    fun monthsMaskFromList(months: List<Boolean>): Int {
      var mask = 0
      for (i in 0 until minOf(12, months.size)) {
        if (months[i]) {
          mask = mask or (1 shl i)
        }
      }
      return mask
    }
  }
}

@JsonClass(generateAdapter = true)
data class AccountBackupDto(
    val id: String? = null,
    val name: String = "",
    val accountName: String = "",
    val password: String = "",
    val flowAmount: String = "",
    val openDate: String = "",
    val streetName: String = "",
    val houseNumber: String = "",
    val notes: String = "",
    val months: List<Boolean> = List(12) { false },
)
