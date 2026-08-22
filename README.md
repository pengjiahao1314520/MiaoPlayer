# 小喵 MiaoPlayer 🐱

一个支持 **在线观看视频** 和 **导入视频源** 的 Android 手机播放器。

## ✨ 功能特性

- 🎬 **在线观看**：直接输入 m3u8 / mp4 / flv / 直播链接播放
- 📺 **视频源管理**：导入 m3u / m3u8 / txt / JSON 接口视频源
- 🔍 **站内搜索**：在已导入的视频源中搜索视频
- ⭐ **收藏/历史**：收藏喜欢的内容，自动记录观看历史
- 🎨 **白色+淡蓝主题**：清爽简洁的 Material Design 3 界面
- 📱 **手机端**：Android 原生应用，支持 API 24+

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3
- **播放器**：ExoPlayer (AndroidX Media3)
- **数据库**：Room
- **网络**：OkHttp
- **图片加载**：Coil

## 📦 项目结构

```
MiaoPlayer/
├── app/
│   └── src/main/
│       ├── java/com/miaomiao/player/
│       │   ├── MainActivity.kt          # 应用入口
│       │   ├── data/
│       │   │   ├── model/Models.kt      # 数据模型
│       │   │   ├── local/AppDatabase.kt # Room 数据库
│       │   │   └── repository/          # 数据仓库
│       │   ├── network/M3UParser.kt     # M3U/TXT/JSON 解析
│       │   ├── player/                  # 播放器
│       │   └── ui/
│       │       ├── screens/             # 界面
│       │       ├── components/          # 通用组件
│       │       └── theme/               # 主题
│       └── res/                         # 资源文件
└── build.gradle.kts
```

## 🚀 构建运行

1. 安装 [Android Studio](https://developer.android.com/studio) (Ladybug 或更高版本)
2. 打开此目录 `C:\Users\dsvs\MiaoPlayer`
3. 等待 Gradle 同步完成（会自动下载依赖）
4. 连接 Android 手机（开启 USB 调试）或使用模拟器
5. 点击 ▶ Run 运行

> **注意**：如果 `gradle-wrapper.jar` 缺失，Android Studio 会自动修复：
> - 打开 Terminal 运行 `gradle wrapper --gradle-version 8.5`
> - 或者直接忽略编译错误，Android Studio 会提示自动获取

## 📖 使用说明

### 在线观看
1. 点击首页的「在线播放」
2. 输入视频地址（如 `https://example.com/video.m3u8`）
3. 点击「播放」即可观看

### 导入视频源
1. 点击底部「视频源」Tab
2. 点击右下角 ➕ 按钮
3. 输入视频源名称和地址
4. 支持格式：
   - `https://example.com/list.m3u` (M3U 直播列表)
   - `https://example.com/list.txt` (TXT 文本列表)
   - `https://example.com/api`   (JSON 接口)
5. 导入后自动解析，点击视频源即可查看视频列表

### 视频操作
- 长按视频卡片 → 收藏/取消收藏
- 播放页点击右上角列表图标 → 查看/切换播放列表
- 点击底部播放/暂停 → 控制播放

## 📄 License

个人学习使用，请勿用于商业用途。