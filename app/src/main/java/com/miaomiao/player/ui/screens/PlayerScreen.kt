package com.miaomiao.player.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.miaomiao.player.data.model.VideoItem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    video: VideoItem,
    videoList: List<VideoItem>,
    onNavigateBack: () -> Unit,
    onNextVideo: (VideoItem) -> Unit,
    onToggleFavorite: (VideoItem) -> Unit
) {
    val context = LocalContext.current
    var currentVideo by remember { mutableStateOf(video) }
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showPlaylist by remember { mutableStateOf(false) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(currentVideo.url))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // 自动隐藏控制栏
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    // 监听播放状态
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // 切换视频
    val switchVideo: (VideoItem) -> Unit = { newVideo ->
        currentVideo = newVideo
        player.stop()
        val mediaItem = MediaItem.fromUri(Uri.parse(newVideo.url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        showControls = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 播放器视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = this@PlayerScreen.player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 顶部控制栏
        if (showControls) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentVideo.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            color = Color.White
                        )
                        if (currentVideo.category.isNotEmpty()) {
                            Text(
                                text = currentVideo.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(onClick = { onToggleFavorite(currentVideo) }) {
                        Icon(
                            imageVector = if (currentVideo.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (currentVideo.isFavorite) Color(0xFFFF80AB) else Color.White
                        )
                    }
                    IconButton(onClick = { showPlaylist = !showPlaylist }) {
                        Icon(Icons.Default.PlaylistPlay, contentDescription = "播放列表", tint = Color.White)
                    }
                }
            }
        }

        // 点击切换显示/隐藏
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 80.dp)
                .clickable { showControls = !showControls }
        )

        // 底部控制栏
        if (showControls) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val currentIdx = videoList.indexOfFirst { it.url == currentVideo.url }
                        if (currentIdx > 0) {
                            switchVideo(videoList[currentIdx - 1])
                        }
                    }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "上一个", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    FilledIconButton(
                        onClick = {
                            player.playWhenReady = !player.playWhenReady
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放"
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(onClick = {
                        val currentIdx = videoList.indexOfFirst { it.url == currentVideo.url }
                        if (currentIdx < videoList.size - 1) {
                            switchVideo(videoList[currentIdx + 1])
                        }
                    }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "下一个", tint = Color.White)
                    }
                }
            }
        }

        // 播放列表侧边栏
        if (showPlaylist) {
            PlaylistPanel(
                currentVideo = currentVideo,
                videoList = videoList,
                onSelectVideo = switchVideo,
                onDismiss = { showPlaylist = false }
            )
        }
    }
}

@Composable
private fun PlaylistPanel(
    currentVideo: VideoItem,
    videoList: List<VideoItem>,
    onSelectVideo: (VideoItem) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .align(Alignment.CenterEnd),
        color = Color.Black.copy(alpha = 0.85f),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "播放列表 (${videoList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            // 列表
            if (videoList.isNotEmpty()) {
                LazyColumn {
                    items(videoList, key = { it.url }) { video ->
                        Surface(
                            onClick = { onSelectVideo(video) },
                            color = if (video.url == currentVideo.url)
                                Color(0xFF64B5F6).copy(alpha = 0.3f)
                            else Color.Transparent
                        ) {
                            Text(
                                text = video.title,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                color = Color.White
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}