package com.miaomiao.player.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.player.data.local.AppDatabase
import com.miaomiao.player.data.model.SourceType
import com.miaomiao.player.data.model.VideoItem
import com.miaomiao.player.data.model.VideoSource
import com.miaomiao.player.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String = "") : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = VideoRepository(db)

    // ===== 状态 =====
    val sources: StateFlow<List<VideoSource>> = repository.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteVideos: StateFlow<List<VideoItem>> = repository.getFavoriteVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentVideos: StateFlow<List<VideoItem>> = repository.getRecentlyPlayed(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList.asStateFlow()

    private val _currentSource = MutableStateFlow<VideoSource?>(null)
    val currentSource: StateFlow<VideoSource?> = _currentSource.asStateFlow()

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 用于取消之前的 collect 任务
    private var videoListJob: kotlinx.coroutines.Job? = null

    // ===== 视频源操作 =====

    fun importSource(name: String, url: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val type = detectSourceType(url)
                val sourceId = repository.addSource(name, url, type)
                // 立即刷新
                refreshSourceById(sourceId)
                _uiState.value = UiState.Success("导入成功")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "导入失败")
            }
        }
    }

    fun deleteSource(source: VideoSource) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    suspend fun refreshSourceById(sourceId: Long) {
        val source = repository.getAllSources().first().find { it.id == sourceId }
            ?: return
        val items = repository.refreshSource(source)
        _videoList.value = items
    }

    fun loadVideosForSource(source: VideoSource) {
        videoListJob?.cancel()
        videoListJob = viewModelScope.launch {
            _currentSource.value = source
            repository.getVideosBySource(source.id)
                .distinctUntilChanged()
                .collect { items ->
                    _videoList.value = items
                }
        }
    }

    fun refreshCurrentSource() {
        val source = _currentSource.value ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val items = repository.refreshSource(source)
                _videoList.value = items
                _uiState.value = UiState.Success("刷新完成，共 ${items.size} 个视频")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "刷新失败")
            }
        }
    }

    // ===== 视频操作 =====

    fun playVideo(video: VideoItem) {
        _currentVideo.value = video
        viewModelScope.launch {
            repository.updatePlayback(video, 0)
        }
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        videoListJob?.cancel()
        videoListJob = viewModelScope.launch {
            if (query.isEmpty()) {
                val source = _currentSource.value
                if (source != null) {
                    repository.getVideosBySource(source.id)
                        .distinctUntilChanged()
                        .collect { _videoList.value = it }
                }
            } else {
                repository.searchVideos(query)
                    .distinctUntilChanged()
                    .collect { _videoList.value = it }
            }
        }
    }

    // ===== 工具函数 =====

    private fun detectSourceType(url: String): SourceType {
        return when {
            url.endsWith(".m3u") || url.endsWith(".m3u8") || url.contains(".m3u") -> SourceType.M3U
            url.endsWith(".txt") -> SourceType.TXT
            url.startsWith("http") && url.contains("json") -> SourceType.API
            url.endsWith(".mp4") || url.endsWith(".flv") || url.endsWith(".ts") -> SourceType.DIRECT
            else -> SourceType.M3U
        }
    }
}