package com.miaomiao.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 视频源（如 m3u 列表、接口订阅）
 */
@Entity(tableName = "video_sources")
data class VideoSource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 源名称
    val url: String,                     // 源地址（m3u/txt/接口）
    val type: SourceType = SourceType.M3U,  // 源类型
    val isActive: Boolean = true,        // 是否启用
    val createdAt: Long = System.currentTimeMillis()
)

enum class SourceType {
    M3U,        // m3u 播放列表
    TXT,        // 文本格式
    API,        // JSON 接口
    DIRECT      // 直接链接
}

/**
 * 单个视频条目
 */
@Entity(tableName = "video_items")
data class VideoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: Long = 0,              // 所属视频源ID
    val title: String,                   // 视频标题
    val url: String,                     // 视频播放地址
    val coverUrl: String = "",           // 封面图
    val duration: String = "",           // 时长 (如 "45:30")
    val category: String = "",           // 分类
    val description: String = "",        // 简介
    val isFavorite: Boolean = false,     // 是否收藏
    val lastPlayedAt: Long = 0,          // 上次播放时间
    val playProgress: Long = 0           // 播放进度(毫秒)
)