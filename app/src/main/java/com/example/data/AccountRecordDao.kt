package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountRecordDao {
  @Query("SELECT * FROM account_records ORDER BY createdAt DESC, localId DESC")
  fun observeAll(): Flow<List<AccountRecordEntity>>

  @Query("SELECT * FROM account_records ORDER BY createdAt DESC, localId DESC")
  suspend fun getAllSnapshot(): List<AccountRecordEntity>

  @Query("SELECT * FROM account_records WHERE externalId = :externalId LIMIT 1")
  suspend fun findByExternalId(externalId: String): AccountRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(record: AccountRecordEntity): Long

  @Update
  suspend fun update(record: AccountRecordEntity)

  @Query("DELETE FROM account_records WHERE localId = :localId")
  suspend fun deleteById(localId: Long)

  @Query("DELETE FROM account_records")
  suspend fun deleteAll()
}
