package com.miaomiao.player.data.local

import androidx.room.*
import com.miaomiao.player.data.model.VideoItem
import com.miaomiao.player.data.model.VideoSource
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoSourceDao {
    @Query("SELECT * FROM video_sources ORDER BY createdAt DESC")
    fun getAllSources(): Flow<List<VideoSource>>

    @Query("SELECT * FROM video_sources WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveSources(): Flow<List<VideoSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: VideoSource): Long

    @Update
    suspend fun updateSource(source: VideoSource)

    @Delete
    suspend fun deleteSource(source: VideoSource)

    @Query("DELETE FROM video_sources WHERE id = :id")
    suspend fun deleteSourceById(id: Long)
}

@Dao
interface VideoItemDao {
    @Query("SELECT * FROM video_items WHERE sourceId = :sourceId ORDER BY title ASC")
    fun getVideosBySource(sourceId: Long): Flow<List<VideoItem>>

    @Query("SELECT * FROM video_items WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM video_items WHERE title LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoItem>>

    @Query("SELECT * FROM video_items ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<VideoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoItem>)

    @Update
    suspend fun updateVideo(video: VideoItem)

    @Delete
    suspend fun deleteVideo(video: VideoItem)

    @Query("DELETE FROM video_items WHERE sourceId = :sourceId")
    suspend fun deleteVideosBySource(sourceId: Long)

    @Query("UPDATE video_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE video_items SET lastPlayedAt = :time, playProgress = :progress WHERE id = :id")
    suspend fun updatePlayback(id: Long, time: Long, progress: Long)
}

@Database(
    entities = [VideoSource::class, VideoItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoSourceDao(): VideoSourceDao
    abstract fun videoItemDao(): VideoItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miao_player_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}