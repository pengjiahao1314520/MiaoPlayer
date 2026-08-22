package com.miaomiao.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miaomiao.player.data.model.VideoItem
import com.miaomiao.player.data.model.VideoSource
import com.miaomiao.player.ui.MainViewModel

sealed class Screen {
    object Home : Screen()
    object Sources : Screen()
    object Settings : Screen()
    data class VideoList(val source: VideoSource) : Screen()
    data class Player(val video: VideoItem, val videoList: List<VideoItem>) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val sources by viewModel.sources.collectAsState()
    val favoriteVideos by viewModel.favoriteVideos.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val videoList by viewModel.videoList.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()
    val currentVideo by viewModel.currentVideo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val isPlayerScreen = currentScreen is Screen.Player

    Scaffold(
        bottomBar = {
            if (!isPlayerScreen) {
                NavigationBar(
                    containerColor = NavigationBarDefaults.containerColor,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                        label = { Text("首页") },
                        selected = currentScreen is Screen.Home,
                        onClick = { currentScreen = Screen.Home }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "视频源") },
                        label = { Text("视频源") },
                        selected = currentScreen is Screen.Sources || currentScreen is Screen.VideoList,
                        onClick = { currentScreen = Screen.Sources }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                        label = { Text("设置") },
                        selected = currentScreen is Screen.Settings,
                        onClick = { currentScreen = Screen.Settings }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        recentVideos = recentVideos,
                        favoriteVideos = favoriteVideos,
                        onPlayVideo = { video ->
                            viewModel.playVideo(video)
                            currentScreen = Screen.Player(video, listOf(video))
                        },
                        onOpenUrl = { url ->
                            val video = VideoItem(title = url.substringAfterLast("/").take(50), url = url)
                            viewModel.playVideo(video)
                            currentScreen = Screen.Player(video, listOf(video))
                        }
                    )
                }

                is Screen.Sources -> {
                    SourceScreen(
                        sources = sources,
                        uiState = uiState,
                        onImportSource = { name, url -> viewModel.importSource(name, url) },
                        onDeleteSource = { viewModel.deleteSource(it) },
                        onOpenSource = { source ->
                            viewModel.loadVideosForSource(source)
                            currentScreen = Screen.VideoList(source)
                        },
                        onRefreshSource = { viewModel.refreshCurrentSource() }
                    )
                }

                is Screen.VideoList -> {
                    VideoListScreen(
                        source = screen.source,
                        videos = videoList,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        onPlayVideo = { video ->
                            viewModel.playVideo(video)
                            currentScreen = Screen.Player(video, videoList)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onNavigateBack = { currentScreen = Screen.Sources }
                    )
                }

                is Screen.Player -> {
                    PlayerScreen(
                        video = screen.video,
                        videoList = screen.videoList,
                        onNavigateBack = {
                            currentScreen = if (currentSource != null)
                                Screen.VideoList(currentSource!!)
                            else Screen.Home
                        },
                        onNextVideo = { viewModel.playVideo(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        onClearCache = { /* 清除缓存 */ },
                        onBackupSources = { /* 导出配置 */ }
                    )
                }
            }
        }
    }
}