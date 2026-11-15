package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AccountRepository(private val dao: AccountRecordDao) {
  private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val listType = Types.newParameterizedType(List::class.java, AccountBackupDto::class.java)
  private val jsonAdapter = moshi.adapter<List<AccountBackupDto>>(listType).indent("  ")

  val allRecords: Flow<List<AccountRecordEntity>> = dao.observeAll()

  suspend fun saveRecord(record: AccountRecordEntity) {
    withContext(Dispatchers.IO) {
      if (record.localId == 0L) {
        dao.insert(record)
      } else {
        dao.update(record.copy(updatedAt = System.currentTimeMillis()))
      }
    }
  }

  suspend fun toggleMonth(record: AccountRecordEntity, monthIndex: Int) {
    withContext(Dispatchers.IO) { dao.update(record.withToggledMonth(monthIndex)) }
  }

  suspend fun deleteRecord(localId: Long) {
    withContext(Dispatchers.IO) { dao.deleteById(localId) }
  }

  suspend fun exportAllToJson(): String =
      withContext(Dispatchers.IO) {
        val current = dao.getAllSnapshot()
        val dtos =
            current.map { entity ->
              AccountBackupDto(
                  id = entity.externalId,
                  name = entity.name,
                  accountName = entity.accountName,
                  password = entity.password,
                  flowAmount = entity.flowAmount,
                  openDate = entity.openDate,
                  streetName = entity.streetName,
                  houseNumber = entity.houseNumber,
                  notes = entity.notes,
                  months = entity.toMonthsList(),
              )
            }
        jsonAdapter.toJson(dtos)
      }

  suspend fun importFromJson(jsonString: String): Int =
      withContext(Dispatchers.IO) {
        val parsed =
            jsonAdapter.fromJson(jsonString)
                ?: throw IllegalArgumentException("Invalid backup JSON")
        var importedCount = 0
        val now = System.currentTimeMillis()
        parsed.forEachIndexed { index, dto ->
          if (dto.name.isBlank() && dto.accountName.isBlank()) return@forEachIndexed
          val extId = dto.id?.takeIf { it.isNotBlank() } ?: "${now}_$index"
          val existing = dao.findByExternalId(extId)
          val entity =
              AccountRecordEntity(
                  localId = existing?.localId ?: 0L,
                  externalId = extId,
                  name = dto.name.trim(),
                  accountName = dto.accountName.trim(),
                  password = dto.password,
                  flowAmount = dto.flowAmount.trim(),
                  openDate = dto.openDate.trim(),
                  streetName = dto.streetName.trim(),
                  houseNumber = dto.houseNumber.trim(),
                  notes = dto.notes.trim(),
                  monthsMask = AccountRecordEntity.monthsMaskFromList(dto.months),
                  createdAt = existing?.createdAt ?: (now - index),
                  updatedAt = now,
              )
          if (existing == null) {
            dao.insert(entity)
          } else {
            dao.update(entity)
          }
          importedCount++
        }
        importedCount
      }
}
