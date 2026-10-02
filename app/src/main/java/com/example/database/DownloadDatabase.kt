package com.example.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
  @PrimaryKey
  val id: String,
  val filename: String,
  val sourceUrl: String,
  val platform: String,
  val mediaType: String,
  val quality: String,
  val fileSize: Long,
  val formattedSize: String,
  val downloadDate: Long = System.currentTimeMillis(),
  val localUriString: String,
  val localFilePath: String? = null,
  val thumbnailUri: String? = null,
  val duration: String? = null
)

@Dao
interface DownloadHistoryDao {
  @Query("SELECT * FROM download_history ORDER BY downloadDate DESC")
  fun getAllHistory(): Flow<List<DownloadHistoryEntity>>

  @Query("SELECT * FROM download_history ORDER BY downloadDate DESC LIMIT :limit")
  fun getRecentHistory(limit: Int): Flow<List<DownloadHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: DownloadHistoryEntity)

  @Query("SELECT * FROM download_history WHERE id = :id")
  suspend fun getById(id: String): DownloadHistoryEntity?

  @Query("DELETE FROM download_history WHERE id = :id")
  suspend fun deleteById(id: String)

  @Delete
  suspend fun delete(item: DownloadHistoryEntity)

  @Query("DELETE FROM download_history")
  suspend fun clearAll()
}

@Database(entities = [DownloadHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun downloadHistoryDao(): DownloadHistoryDao
}
