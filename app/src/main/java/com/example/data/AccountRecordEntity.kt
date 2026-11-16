package com.example.data

data class AccountRecordEntity(
    val localId: Long = 0L,
    val externalId: String = "",
    val name: String = "",
    val accountName: String = "",
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

    fun paidMonthsCount(): Int = (0..11).count { isMonthPaid(it) }

    fun unpaidMonthsCount(): Int = 12 - paidMonthsCount()

    fun toMonthsList(): List<Boolean> = List(12) { index -> isMonthPaid(index) }

    fun withToggledMonth(monthIndex: Int): AccountRecordEntity {
        if (monthIndex !in 0..11) return this
        val newMask = monthsMask xor (1 shl monthIndex)
        return copy(monthsMask = newMask, updatedAt = System.currentTimeMillis())
    }

    companion object {
        fun monthsMaskFromList(months: List<Boolean>): Int {
            var mask = 0
            for (i in 0..11) {
                if (months.getOrElse(i) { false }) {
                    mask = mask or (1 shl i)
                }
            }
            return mask
        }
    }
}
