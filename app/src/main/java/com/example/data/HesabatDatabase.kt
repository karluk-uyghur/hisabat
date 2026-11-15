package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AccountRecordEntity::class], version = 1, exportSchema = false)
abstract class HesabatDatabase : RoomDatabase() {
  abstract fun accountRecordDao(): AccountRecordDao

  companion object {
    @Volatile private var instance: HesabatDatabase? = null

    fun getInstance(context: Context): HesabatDatabase {
      return instance
          ?: synchronized(this) {
            instance
                ?: Room.databaseBuilder(
                        context.applicationContext,
                        HesabatDatabase::class.java,
                        "hesabat_database",
                    )
                    .build()
                    .also { instance = it }
          }
    }
  }
}
