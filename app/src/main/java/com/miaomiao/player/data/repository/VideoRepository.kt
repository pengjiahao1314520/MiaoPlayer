package com.miaomiao.player.data.repository

import com.miaomiao.player.data.local.AppDatabase
import com.miaomiao.player.data.model.SourceType
import com.miaomiao.player.data.model.VideoItem
import com.miaomiao.player.data.model.VideoSource
import com.miaomiao.player.network.M3UParser
import kotlinx.coroutines.flow.Flow

class VideoRepository(private val db: AppDatabase) {

    private val sourceDao = db.videoSourceDao()
    private val videoDao = db.videoItemDao()

    // ===== 视频源操作 =====

    fun getAllSources(): Flow<List<VideoSource>> = sourceDao.getAllSources()

    fun getActiveSources(): Flow<List<VideoSource>> = sourceDao.getActiveSources()

    suspend fun addSource(name: String, url: String, type: SourceType = SourceType.M3U): Long {
        return sourceDao.insertSource(
            VideoSource(
                name = name,
                url = url,
                type = type
            )
        )
    }

    suspend fun deleteSource(source: VideoSource) {
        sourceDao.deleteSource(source)
        videoDao.deleteVideosBySource(source.id)
    }

    suspend fun refreshSource(source: VideoSource): List<VideoItem> {
        // 先删除旧的
        videoDao.deleteVideosBySource(source.id)

        // 根据类型解析
        val items = when (source.type) {
            SourceType.M3U -> M3UParser.parseUrl(source.url)
            SourceType.TXT -> M3UParser.parseTxtUrl(source.url)
            SourceType.API -> M3UParser.parseApiUrl(source.url)
            SourceType.DIRECT -> listOf(
                VideoItem(
                    title = source.url.substringAfterLast("/").take(50),
                    url = source.url,
                    category = "直接链接"
                )
            )
        }

        // 绑定 sourceId 并存入数据库
        val videos = items.map { it.copy(sourceId = source.id) }
        videoDao.insertVideos(videos)
        return videos
    }

    // ===== 视频操作 =====

    fun getVideosBySource(sourceId: Long): Flow<List<VideoItem>> =
        videoDao.getVideosBySource(sourceId)

    fun getFavoriteVideos(): Flow<List<VideoItem>> =
        videoDao.getFavoriteVideos()

    fun getRecentlyPlayed(limit: Int = 20): Flow<List<VideoItem>> =
        videoDao.getRecentlyPlayed(limit)

    fun searchVideos(query: String): Flow<List<VideoItem>> =
        videoDao.searchVideos(query)

    suspend fun toggleFavorite(video: VideoItem) {
        videoDao.toggleFavorite(video.id, !video.isFavorite)
    }

    suspend fun updatePlayback(video: VideoItem, progress: Long) {
        videoDao.updatePlayback(video.id, System.currentTimeMillis(), progress)
    }
}