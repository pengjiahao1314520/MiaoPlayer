package com.miaomiao.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.miaomiao.player.data.model.VideoSource
import com.miaomiao.player.ui.components.VideoSourceCard
import com.miaomiao.player.ui.UiState
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceScreen(
    sources: List<VideoSource>,
    uiState: UiState,
    onImportSource: (String, String) -> Unit,
    onDeleteSource: (VideoSource) -> Unit,
    onOpenSource: (VideoSource) -> Unit,
    onRefreshSource: () -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 提示状态（只提示一次）
    var lastMessage by remember { mutableStateOf("") }
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                if (uiState.message != lastMessage && uiState.message.isNotEmpty()) {
                    lastMessage = uiState.message
                    Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                }
            }
            is UiState.Error -> {
                if (uiState.message != lastMessage && uiState.message.isNotEmpty()) {
                    lastMessage = uiState.message
                    Toast.makeText(context, uiState.message, Toast.LENGTH_LONG).show()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频源管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onRefreshSource) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImportDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "导入视频源")
            }
        }
    ) { padding ->
        if (sources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无视频源",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "点击右下角 + 导入 m3u / txt / 接口",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "共 ${sources.size} 个视频源，点击源可查看视频列表",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(sources, key = { it.id }) { source ->
                    VideoSourceCard(
                        name = source.name,
                        url = source.url,
                        active = source.isActive,
                        onClick = { onOpenSource(source) },
                        onDelete = { onDeleteSource(source) }
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        ImportSourceDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { name, url ->
                if (name.isNotBlank() && url.isNotBlank()) {
                    onImportSource(name.trim(), url.trim())
                }
                showImportDialog = false
            }
        )
    }
}

@Composable
fun ImportSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入视频源") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("视频源名称") },
                    placeholder = { Text("例如：我的直播源") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("视频源地址") },
                    placeholder = { Text("https://.../list.m3u 或 /list.txt 或 JSON接口") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    minLines = 2
                )
                Text(
                    text = "支持：m3u/m3u8 直播列表、txt 文本列表、JSON 接口",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, url) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) { Text("导入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}