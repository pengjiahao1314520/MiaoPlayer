package com.miaomiao.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miaomiao.player.data.model.VideoItem
import com.miaomiao.player.ui.components.VideoItemCard
import com.miaomiao.player.ui.theme.PrimaryContainer
import com.miaomiao.player.ui.theme.OnPrimaryContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    recentVideos: List<VideoItem>,
    favoriteVideos: List<VideoItem>,
    onPlayVideo: (VideoItem) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 顶部欢迎横幅
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainer)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "小喵Player",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "在线观看 · 视频源导入 · 高清画质",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 快捷播放按钮
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showUrlDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = PrimaryOnPrimaryColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("在线播放")
                        }
                    }
                }
            }
        }

        // 最近播放
        if (recentVideos.isNotEmpty()) {
            item {
                SectionHeader("最近播放")
            }
            items(recentVideos, key = { it.id }) { video ->
                VideoItemCard(video = video, onClick = { onPlayVideo(video) })
            }
        }

        // 收藏
        if (favoriteVideos.isNotEmpty()) {
            item {
                SectionHeader("我的收藏")
            }
            items(favoriteVideos, key = { it.id }) { video ->
                VideoItemCard(video = video, onClick = { onPlayVideo(video) })
            }
        }

        // 空状态
        if (recentVideos.isEmpty() && favoriteVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "还没有播放记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "去视频源页导入你的视频源吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // URL 播放对话框
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("在线播放") },
            text = {
                Column {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("输入视频地址") },
                        placeholder = { Text("https://...m3u8 或 .mp4") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            onOpenUrl(urlInput.trim())
                            showUrlDialog = false
                            urlInput = ""
                        }
                    }
                ) { Text("播放") }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("取消") }
            }
        )
    }
}

private val PrimaryOnPrimaryColor = Color(0xFF1565C0)

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        color = MaterialTheme.colorScheme.primary
    )
}