package com.jellytunes.tv.ui.player

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jellytunes.tv.ui.lyrics.LyricsViewCompact
import com.jellytunes.tv.ui.theme.JellyTunesColors
import com.jellytunes.tv.ui.theme.JellyTunesTypography
import com.jellytunes.tv.ui.theme.LocalJellyTunesColors
import kotlin.math.abs
import kotlin.math.roundToInt

// 拖动方向枚举
enum class DragDirection {
    HORIZONTAL, VERTICAL
}

@Composable
fun MobilePlayerScreen(
    playerState: PlayerState,
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onThemeChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJellyTunesColors.current
    val density = LocalDensity.current
    
    // 手势检测变量 - 只保留垂直滑动
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // 显著降低滑动阈值，提高响应灵敏度
    val dragThreshold = with(density) { 15.dp.toPx() }
    
    // 动画背景色
    val animatedBackground by animateColorAsState(
        targetValue = colors.background,
        animationSpec = tween(500),
        label = "background"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBackground)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { position ->
                        isDragging = true
                        verticalDragOffset = 0f
                        println("📱 滑动开始 at (${position.x}, ${position.y})")
                    },
                    onDragEnd = {
                        isDragging = false
                        println("📱 滑动结束 - 偏移量: $verticalDragOffset, 阈值: $dragThreshold")
                        
                        // 只处理垂直滑动 - 切换歌曲
                        if (abs(verticalDragOffset) > dragThreshold) {
                            if (verticalDragOffset > 0) {
                                println("⏮️ 向下滑动 - 切换到上一首")
                                onPreviousTrack() // 向下滑动切到上一首
                            } else {
                                println("⏭️ 向上滑动 - 切换到下一首")
                                onNextTrack() // 向上滑动切到下一首
                            }
                        } else {
                            println("📱 滑动距离不足，不触发切换")
                        }
                        
                        // 重置状态
                        verticalDragOffset = 0f
                    }
                ) { change, dragAmount ->
                    val (_, dy) = dragAmount
                    // 只累积垂直方向的偏移量
                    verticalDragOffset += dy
                    // 添加实时调试输出
                    if (abs(verticalDragOffset) > 5f) { // 每5像素输出一次
                        println("📱 实时滑动偏移: $verticalDragOffset")
                    }
                    change.consume()
                }
            }
    ) {
        // 整屏内容区域 - 只支持垂直滑动
        val totalOffsetY = if (isDragging) verticalDragOffset else 0f
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, totalOffsetY.roundToInt()) }
        ) {
            AnimatedContent(
                targetState = Pair(playerState.currentTrack?.id, colors.name),
                transitionSpec = {
                    if (targetState.first != initialState.first) {
                        // 歌曲切换时的垂直滑动效果（上下滑动切歌）
                        (slideInVertically { height -> height } + fadeIn(tween(400)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(tween(300)))
                    } else {
                        // 主题切换时只使用淡入淡出效果，不滑动
                        fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
                    }
                },
                label = "full_screen_content"
            ) { (trackId, themeName) ->
                // 重新获取当前状态以确保数据一致性
                val currentTrack = playerState.currentTrack
                val currentColors = LocalJellyTunesColors.current
                
                FullScreenContent(
                    playerState = playerState,
                    colors = currentColors,
                    track = currentTrack
                )
            }
        }
    }
}

@Composable
private fun FullScreenContent(
    playerState: PlayerState,
    colors: JellyTunesColors,
    track: Track?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 顶部区域 - 品牌和主题指示器
        HeaderSection(colors = colors)
        
        // 中间主要内容区域 - 向上调整位置
        MainContentSection(
            track = track,
            colors = colors,
            modifier = Modifier
                .weight(0.65f)
                .padding(top = 8.dp) // 向上调整，减少顶部间距
        )
        
        // 歌词区域
        if (playerState.showLyrics) {
            LyricsViewCompact(
                lyrics = playerState.lyrics,
                currentPositionMs = playerState.currentPositionMs,
                modifier = Modifier
                    .weight(0.15f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
        
        // 底部控制区域
        BottomControlSection(
            playerState = playerState,
            colors = colors,
            onPlayPauseToggle = { /* 在外层处理 */ },
            onPreviousTrack = { /* 在外层处理 */ },
            onNextTrack = { /* 在外层处理 */ }
        )
    }
}

@Composable
private fun HeaderSection(colors: JellyTunesColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "JELLYTUNES",
            style = JellyTunesTypography.brand.copy(
                color = colors.textMuted
            )
        )
        
        Text(
            text = colors.name.uppercase(),
            style = JellyTunesTypography.brand.copy(
                color = colors.primary
            )
        )
    }
}

@Composable
private fun MainContentSection(
    track: Track?,
    colors: JellyTunesColors,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val albumSize = (screenWidth * 0.85f).coerceAtMost(380.dp) // 进一步增大专辑图片
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),  // 减少高度为歌曲信息留出空间
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 大尺寸专辑封面
        AlbumCoverImage(
            track = track,
            colors = colors,
            size = albumSize
        )
        
        Spacer(modifier = Modifier.height(16.dp)) // 减少间距
        
        // 完整的歌曲信息
        TrackInfoSection(track = track, colors = colors)
    }
}

@Composable
private fun AlbumCoverImage(
    track: Track?,
    colors: JellyTunesColors,
    size: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 36.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = colors.primary.copy(alpha = 0.4f),
                spotColor = colors.primary.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (track != null) {
            var showDefault by remember { mutableStateOf(false) }
            
            // Check for embedded album art first
            val albumArtBitmap = remember(track.id, track.albumArtData) {
                track.albumArtData?.let { data ->
                    try {
                        BitmapFactory.decodeByteArray(data, 0, data.size)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            if (albumArtBitmap != null) {
                Image(
                    bitmap = albumArtBitmap.asImageBitmap(),
                    contentDescription = "Album cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                var imageUrl by remember(track.id) { 
                    mutableStateOf(track.albumArtUrl ?: track.artistImageUrl) 
                }

                if (imageUrl != null && !showDefault) {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(500)
                            .build()
                    )

                    when (painter.state) {
                        is AsyncImagePainter.State.Error -> {
                            if (imageUrl == track.albumArtUrl && track.artistImageUrl != null) {
                                imageUrl = track.artistImageUrl
                            } else {
                                showDefault = true
                            }
                        }
                        else -> {
                            Image(
                                painter = painter,
                                contentDescription = "Album cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                if (showDefault || imageUrl == null) {
                    DefaultMobileAlbumCover(colors = colors)
                }
            }
        } else {
            DefaultMobileAlbumCover(colors = colors)
        }
    }
}

@Composable
private fun DefaultMobileAlbumCover(colors: JellyTunesColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.coverGradientEnd,
                        colors.coverGradientMid,
                        colors.coverGradientStart
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(110.dp),
            tint = colors.primary.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun TrackInfoSection(track: Track?, colors: JellyTunesColors) {
    track?.let { currentTrack ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 歌曲标题
            Text(
                text = currentTrack.title,
                style = JellyTunesTypography.trackTitle.copy(
                    color = colors.textPrimary,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 艺术家
            Text(
                text = currentTrack.artist,
                style = JellyTunesTypography.artistName.copy(
                    color = colors.textSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 专辑
            Text(
                text = currentTrack.album,
                style = JellyTunesTypography.albumName.copy(
                    color = colors.textMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BottomControlSection(
    playerState: PlayerState,
    colors: JellyTunesColors,
    onPlayPauseToggle: () -> Unit,
    onPreviousTrack: () -> Unit,
    onNextTrack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度条 - 现在放在播放按钮上方
        ProgressSection(
            currentPositionMs = playerState.currentPositionMs,
            durationMs = playerState.durationMs,
            colors = colors
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 控制按钮
        ControlButtonsSection(
            isPlaying = playerState.isPlaying,
            colors = colors,
            onPlayPauseToggle = onPlayPauseToggle,
            onPreviousTrack = onPreviousTrack,
            onNextTrack = onNextTrack
        )
    }
}

@Composable
private fun ProgressSection(
    currentPositionMs: Long,
    durationMs: Long,
    colors: JellyTunesColors
) {
    val progress by animateFloatAsState(
        targetValue = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f,
        animationSpec = tween(100),
        label = "progress"
    )

    val animatedTrack by animateColorAsState(
        targetValue = colors.progressTrack,
        animationSpec = tween(500),
        label = "track"
    )

    val animatedIndicator by animateColorAsState(
        targetValue = colors.progressIndicator,
        animationSpec = tween(500),
        label = "indicator"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(animatedTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .background(animatedIndicator)
            )
            
            if (progress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .drawBehind {
                                drawCircle(
                                    color = colors.progressGlow,
                                    radius = size.minDimension
                                )
                                drawCircle(
                                    color = colors.primaryLight,
                                    radius = size.minDimension / 3
                                )
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPositionMs),
                style = JellyTunesTypography.timeLabel.copy(
                    color = colors.textMuted
                )
            )
            Text(
                text = formatTime(durationMs),
                style = JellyTunesTypography.timeLabel.copy(
                    color = colors.textMuted
                )
            )
        }
    }
}

@Composable
private fun ControlButtonsSection(
    isPlaying: Boolean,
    colors: JellyTunesColors,
    onPlayPauseToggle: () -> Unit,
    onPreviousTrack: () -> Unit,
    onNextTrack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button (居中显示)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.primary, colors.primaryLight)
                    )
                )
                .clickable(
                    onClick = { 
                        println("⏯️ 播放/暂停按钮被点击")
                        onPlayPauseToggle()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp),
                tint = colors.textPrimary
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}