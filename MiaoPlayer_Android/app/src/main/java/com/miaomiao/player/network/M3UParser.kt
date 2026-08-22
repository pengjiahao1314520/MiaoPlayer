package com.miaomiao.player.network

import com.miaomiao.player.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * M3U 播放列表解析器
 * 支持标准 m3u8 格式和 m3u 格式
 */
object M3UParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun parseUrl(url: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:120.0)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw Exception("Empty response")
            parseM3UContent(body, url)
        } catch (e: Exception) {
            // 尝试作为直接视频源处理
            listOf(
                VideoItem(
                    title = url.substringAfterLast("/").take(50),
                    url = url,
                    category = "直接链接"
                )
            )
        }
    }

    /**
     * 解析 m3u 格式文本
     */
    private fun parseM3UContent(content: String, baseUrl: String): List<VideoItem> {
        val items = mutableListOf<VideoItem>()
        val lines = content.lines()
        var currentTitle = ""
        var currentCover = ""
        var currentDuration = ""
        var currentCategory = ""

        for (line in lines) {
            val trimmed = line.trim()
            when {
                // EXTM3U 头
                trimmed.startsWith("#EXTM3U") -> { /* 跳过 */ }

                // 扩展信息行: #EXTINF:-1 tvg-id="" tvg-logo="..." group-title="...",频道名称
                trimmed.startsWith("#EXTINF:") -> {
                    currentDuration = trimmed.substringAfter("#EXTINF:").substringBefore(",")
                    // 提取 logo
                    currentCover = if (trimmed.contains("tvg-logo=\"")) {
                        trimmed.substringAfter("tvg-logo=\"").substringBefore("\"")
                    } else ""

                    // 提取分类
                    currentCategory = if (trimmed.contains("group-title=\"")) {
                        trimmed.substringAfter("group-title=\"").substringBefore("\"")
                    } else "未分类"

                    // 提取标题 (逗号后面)
                    currentTitle = trimmed.substringAfterLast(",").trim()
                }

                // #KODIPROP 等元数据
                trimmed.startsWith("#") -> { /* 跳过 */ }

                // 实际 URL 行
                trimmed.isNotEmpty() -> {
                    val url = resolveUrl(trimmed, baseUrl)
                    if (url.isNotEmpty()) {
                        items.add(
                            VideoItem(
                                title = currentTitle.ifEmpty { url.substringAfterLast("/").take(50) },
                                url = url,
                                coverUrl = currentCover,
                                duration = currentDuration,
                                category = currentCategory.ifEmpty { "未分类" }
                            )
                        )
                        // 重置
                        currentTitle = ""
                        currentCover = ""
                        currentDuration = ""
                        currentCategory = ""
                    }
                }
            }
        }
        return items
    }

    /**
     * 解析 TXT 格式（每行一个视频链接）
     */
    suspend fun parseTxtUrl(url: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            body.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .map { line ->
                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        VideoItem(
                            title = parts[0].trim(),
                            url = parts[1].trim(),
                            category = "TXT导入"
                        )
                    } else {
                        VideoItem(
                            title = line.substringAfterLast("/").take(50),
                            url = line,
                            category = "TXT导入"
                        )
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 解析 JSON 接口格式
     * 期望格式: [{"title":"xxx","url":"xxx","cover":"xxx","category":"xxx"}]
     */
    suspend fun parseApiUrl(url: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            val items = mutableListOf<VideoItem>()
            val jsonArray = org.json.JSONArray(body)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    VideoItem(
                        title = obj.optString("title", "未知"),
                        url = obj.getString("url"),
                        coverUrl = obj.optString("cover", ""),
                        category = obj.optString("category", "未分类"),
                        description = obj.optString("description", "")
                    )
                )
            }
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun resolveUrl(url: String, baseUrl: String): String {
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        if (url.startsWith("//")) return "https:$url"
        // 相对路径
        val base = baseUrl.substringBeforeLast("/")
        return "$base/$url"
    }
}