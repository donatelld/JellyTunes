# JellyTunes

JellyTunes 是一个专为 Android TV 设计的音乐播放器应用，集成了 Jellyfin 媒体服务器，提供流畅的音乐播放体验。

## 🎵 功能特性

- **Jellyfin 集成**：直接连接到 Jellyfin 媒体服务器
- **Android TV 优化**：专为大屏电视界面设计
- **流媒体播放**：支持在线音频流播放
- **主题切换**：多种配色主题可选
- **遥控器控制**：完美适配电视遥控器操作
- **随机播放**：支持音乐随机播放模式

## 🚀 技术栈

- **Kotlin** - 主要开发语言
- **Jetpack Compose** - 现代 UI 框架
- **Compose for TV** - Android TV 专用组件库
- **ExoPlayer** - 高性能音频播放器 (Media3)
- **Retrofit** - 网络请求库
- **Coil** - 图片加载库

## 📱 应用信息

- **包名**: `com.jellytunes.tv`
- **版本**: 1.0.0
- **最低 SDK**: 21 (Android 5.0)
- **目标 SDK**: 34 (Android 14)
- **Java 版本**: 17

## 🛠️ 开发环境

### 必需工具
- Android Studio (最新版本推荐)
- Android SDK 34
- JDK 17
- Git

### 构建项目
```bash
# 克隆项目
git clone https://github.com/donatelld/JellyTunes.git
cd JellyTunes

# 同步 Gradle 依赖
./gradlew build

# 运行在模拟器
./gradlew installDebug

# 生成发布版 APK
./gradlew assembleRelease
```

### 签名配置
项目已配置自动签名，相关信息：
- 密钥库: `jellytunes-release-key.jks`
- 别名: `jellytunes-key`
- 密码: `jellytunes123`

## 📁 项目结构

```
JellyTunes/
├── app/
│   ├── src/main/
│   │   ├── java/com/jellytunes/tv/
│   │   │   ├── data/
│   │   │   │   ├── api/           # Jellyfin API 接口
│   │   │   │   ├── config/        # 应用配置
│   │   │   │   ├── model/         # 数据模型
│   │   │   │   └── repository/    # 数据仓库
│   │   │   ├── service/           # 音频播放服务
│   │   │   ├── ui/
│   │   │   │   ├── player/        # 播放器界面
│   │   │   │   └── theme/         # 主题样式
│   │   │   ├── JellyTunesApp.kt   # 应用入口
│   │   │   └── MainActivity.kt    # 主活动
│   │   ├── res/                   # 资源文件
│   │   │   ├── mipmap-*/          # 应用图标
│   │   │   ├── drawable/          # 图片资源
│   │   │   └── xml/               # 配置文件
│   │   └── AndroidManifest.xml    # 应用清单
│   └── build.gradle.kts           # 模块构建配置
├── build.gradle.kts               # 项目构建配置
└── settings.gradle.kts            # 项目设置
```

## 🔧 主要组件

### 核心类说明

- **MainActivity.kt** - 应用主入口，管理 UI 和播放服务
- **AudioPlaybackService.kt** - 音频播放核心服务
- **JellyfinApiService.kt** - Jellyfin API 客户端
- **PlayerScreen.kt** - 播放器用户界面
- **AppConfig.kt** - 应用全局配置

### 主题系统
支持多种配色主题，通过 `ThemeType` 枚举进行切换：
- AMBER (琥珀色)
- BLUE (蓝色)
- GREEN (绿色)
- PURPLE (紫色)
- RED (红色)

## 🎨 UI 特性

- **响应式设计**：适配不同屏幕尺寸
- **焦点导航**：优化遥控器操作体验
- **动画效果**：平滑的界面过渡动画
- **深色主题**：默认使用深色模式
- **高清图标**：多分辨率应用图标支持

## 📦 发布版本

生成的 APK 文件命名格式：`JellyTunes_1.0.0.apk`

可通过以下命令生成：
```bash
./gradlew assembleRelease
```

APK 文件位置：`app/build/outputs/apk/release/JellyTunes_1.0.0.apk`

## 🔒 安全说明

⚠️ **重要提醒**：
当前版本中密码以明文形式存储在代码中，仅用于开发测试。
生产环境中应使用安全的凭证存储方案。

## 🐛 已知问题

- 暂不支持离线播放
- 不支持播放列表自定义排序
- 缺少搜索功能

## 📝 更新日志

### v1.0.0 (2024-02-04)
- 初始版本发布
- 实现 Jellyfin 基础集成
- 完成播放器核心功能
- 添加主题切换功能
- 优化 Android TV 用户体验

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues: [https://github.com/donatelld/JellyTunes/issues](https://github.com/donatelld/JellyTunes/issues)
- 邮箱: [your-email@example.com]

---
*最后更新时间：2024年2月*