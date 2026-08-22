package com.miaomiao.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClearCache: () -> Unit,
    onBackupSources: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("设置") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // 关于
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "小喵 MiaoPlayer",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "v1.0.0 · 支持在线播放与视频源导入",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 功能说明
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "支持格式",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 在线视频：m3u8 / mp4 / flv / DASH / 直播流")
                Text("• 视频源：m3u / m3u8 / txt / JSON 接口")
                Text("• 本地播放：通过文件管理器选择视频")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 操作按钮
        Button(
            onClick = onBackupSources,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出视频源配置")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onClearCache,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("清除播放缓存")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/azxcvn/mpv-android-anime4k")
                    )
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("关于 Anime4K 画质增强")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "小喵Player - 让看视频更简单",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}