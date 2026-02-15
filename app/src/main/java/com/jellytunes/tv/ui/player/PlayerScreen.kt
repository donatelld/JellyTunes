package com.jellytunes.tv.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jellytunes.tv.ui.theme.JellyTunesColors
import com.jellytunes.tv.ui.theme.JellyTunesTypography
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.jellytunes.tv.data.model.LyricsLine
import com.jellytunes.tv.ui.theme.LocalJellyTunesColors

@Composable
fun PlayerScreen(
    playerState: PlayerState,
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onThemeChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalJellyTunesColors.current
    val focusRequester = remember { FocusRequester() }

    // Animate background color transition
    val animatedBackground by animateColorAsState(
        targetValue = colors.background,
        animationSpec = tween(500),
        label = "background"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBackground)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionCenter, Key.Enter -> {
                            onPlayPauseToggle()
                            true
                        }
                        Key.DirectionRight -> {
                            onNextTrack()
                            true
                        }
                        Key.DirectionLeft -> {
                            onPreviousTrack()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side - Large Album Cover (55% width)
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = playerState.currentTrack,
                    transitionSpec = {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(300)))
                    },
                    label = "album_cover"
                ) { track ->
                    AlbumCoverLarge(
                        track = track,
                        colors = colors,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                    )
                }
                
                // Right edge gradient fade into background
                val gradientEnd by animateColorAsState(
                    targetValue = colors.gradientOverlayEnd,
                    animationSpec = tween(500),
                    label = "gradient"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, gradientEnd)
                            )
                        )
                )
            }

            // Right side - Track info, lyrics and controls (45% width)
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top padding section
                    Spacer(modifier = Modifier.weight(0.05f))  // 5% 顶部空白
                    
                    // Top section - Song information (optimized for single line title)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.35f)  // 增加到35%
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly  // 均匀分布间距
                        ) {
                            playerState.currentTrack?.let { track ->
                                // Track title - horizontal scrolling implementation
                                HorizontalMarqueeText(
                                    text = track.title,
                                    style = JellyTunesTypography.trackTitle.copy(
                                        color = colors.textPrimary,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            offset = Offset(0f, 4f),
                                            blurRadius = 8f
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Artist
                                Text(
                                    text = track.artist,
                                    style = JellyTunesTypography.artistName.copy(
                                        color = colors.textSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Album
                                Text(
                                    text = track.album,
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

                    // Middle section - Lyrics display (fixed layout for consistent positioning)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.25f)  // 减少到25%
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 固定高度的容器确保第一行歌词始终可见
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)  // 给整体内容一些内边距
                        ) {
                            if (playerState.currentLyrics.isEmpty()) {
                                // 无歌词时的显示
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无歌词",
                                        style = JellyTunesTypography.hint.copy(
                                            color = colors.textMuted,
                                            fontSize = JellyTunesTypography.hint.fontSize * 1.1f
                                        )
                                    )
                                }
                            } else {
                                // 有歌词时的固定布局显示
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // 显示最多3行歌词，每行固定高度
                                    val displayLyrics = if (playerState.currentLyrics.size <= 3) {
                                        playerState.currentLyrics
                                    } else {
                                        // 如果超过3行，显示当前行及相邻行
                                        val currentIndex = playerState.currentLyricIndex ?: 0
                                        val startIndex = (currentIndex - 1).coerceAtLeast(0)
                                        val endIndex = (startIndex + 2).coerceAtMost(playerState.currentLyrics.size - 1)
                                        playerState.currentLyrics.subList(startIndex, endIndex + 1)
                                    }
                                    
                                    // 确保始终显示3行（不足时用空行补充）
                                    val paddedLyrics = displayLyrics + List(3 - displayLyrics.size) { LyricsLine(0L, "") }
                                    
                                    paddedLyrics.take(3).forEachIndexed { index, lyric ->
                                        val isCurrent = when {
                                            playerState.currentLyrics.size <= 3 -> index == (playerState.currentLyricIndex ?: 0)
                                            else -> {
                                                val currentIndex = playerState.currentLyricIndex ?: 0
                                                val displayIndex = (currentIndex - (playerState.currentLyricIndex ?: 0) + 1).coerceAtLeast(0)
                                                index == displayIndex
                                            }
                                        }
                                        
                                        val textColor = if (isCurrent) colors.primary else colors.textSecondary
                                        val fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        val scaleFactor = if (isCurrent) 1.05f else 0.9f
                                        
                                        Text(
                                            text = lyric.value.ifEmpty { "♪" },
                                            style = JellyTunesTypography.artistName.copy(
                                                color = textColor,
                                                fontWeight = fontWeight,
                                                fontSize = JellyTunesTypography.artistName.fontSize * scaleFactor
                                            ),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)  // 每行等高分配
                                                .padding(vertical = 2.dp),
                                            maxLines = 2,  // 允许每行最多2行显示
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom section - Controls (increased height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)  // 保持40%
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Play/Pause button with glow effect
                            PlayPauseButton(
                                isPlaying = playerState.isPlaying,
                                colors = colors,
                                onClick = onPlayPauseToggle
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Progress bar
                            ProgressSection(
                                currentPositionMs = playerState.currentPositionMs,
                                durationMs = playerState.durationMs,
                                colors = colors
                            )
                        }
                    }
                }
            }
        }

        // 移除了右下角的 JELLYTUNES 品牌标识
    }
}

@Composable
private fun AlbumCoverLarge(
    track: Track?,
    colors: JellyTunesColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(0.dp),
                ambientColor = colors.primary.copy(alpha = 0.3f),
                spotColor = colors.primary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (track != null) {
            var showDefault by remember { mutableStateOf(false) }
            var imageUrl by remember(track.id) { 
                mutableStateOf(track.albumArtUrl ?: track.artistImageUrl) 
            }

            if (imageUrl != null && !showDefault) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(500)
                        .placeholder(android.R.drawable.screen_background_dark_transparent)  // 透明占位符
                        .fallback(android.R.drawable.screen_background_dark_transparent)     // 透明后备
                        .error(android.R.drawable.screen_background_dark_transparent)        // 透明错误图
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
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            alpha = 1f  // 确保完全不透明
                        )
                    }
                }
            }

            if (showDefault || imageUrl == null) {
                DefaultAlbumCover(colors = colors)
            }
        } else {
            DefaultAlbumCover(colors = colors)
        }
    }
}

@Composable
private fun DefaultAlbumCover(colors: JellyTunesColors) {
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
        // Vinyl record aesthetic - concentric circles hint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            tint = colors.primary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    colors: JellyTunesColors,
    onClick: () -> Unit
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.7f else 0.4f,
        animationSpec = tween(300),
        label = "glow"
    )

    val animatedPrimary by animateColorAsState(
        targetValue = colors.primary,
        animationSpec = tween(500),
        label = "primary"
    )

    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 1.15f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // 旋转动画（仅用于播放状态切换时）
    var rotationTarget by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(200),
        label = "rotation"
    )
    
    // 脉冲动画（仅在播放时）
    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = if (isPlaying) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        },
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .size(96.dp)
            .graphicsLayer {
                scaleX = scale * pulseScale
                scaleY = scale * pulseScale
                rotationZ = rotation
            }
            .drawBehind {
                // 多层光晕效果
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        radius = size.minDimension * 0.8f
                    )
                )
                
                // 内层光晕（更强的发光效果）
                if (isPlaying) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedPrimary.copy(alpha = glowAlpha * 0.6f),
                                Color.Transparent
                            ),
                            radius = size.minDimension * 0.4f
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 外层环形装饰
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.2f),
                            colors.primaryLight.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colors.primary, colors.primaryLight)
                    ),
                    shape = CircleShape
                )
        )
        
        // 主按钮
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.primary,
                            colors.primaryLight.copy(alpha = 0.8f)
                        )
                    )
                )
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = animatedPrimary.copy(alpha = 0.3f),
                    spotColor = animatedPrimary.copy(alpha = 0.4f)
                )
                .clickable { 
                    // 触发旋转动画
                    rotationTarget += 180f
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp),
                tint = colors.textPrimary
            )
            
            // 播放时的小音符装饰
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp),
                    tint = colors.textPrimary.copy(alpha = 0.7f)
                )
            }
        }
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
            .padding(horizontal = 32.dp)
    ) {
        // Custom progress bar with glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(animatedTrack)
        ) {
            // Progress fill with glow
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(animatedIndicator)
            )
            
            // Glow dot at progress position
            if (progress > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
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

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
fun HorizontalMarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var textWidthPx by remember { mutableStateOf(0) }
    var containerWidthPx by remember { mutableStateOf(0) }
    // 更宽松的滚动条件：只要超出显示区域就滚动
    val needsScrolling = textWidthPx > containerWidthPx
    
    var offset by remember { mutableStateOf(0f) }
    var direction by remember { mutableStateOf(1) } // 1向右，-1向左
    
    // 只有需要滚动时才启动动画
    LaunchedEffect(needsScrolling) {
        if (needsScrolling) {
            while (true) {
                if (direction == 1) {
                    // 向右滚动（较小幅度，确保不会超出边界）
                    offset += 1f
                    if (offset >= 80f) {
                        direction = -1
                        delay(800) // 在右侧停留0.8秒
                    }
                } else {
                    // 向左滚动
                    offset -= 1f
                    if (offset <= -80f) {
                        direction = 1
                        delay(800) // 在左侧停留0.8秒
                    }
                }
                delay(25) // 稍快的滚动速度
            }
        } else {
            offset = 0f // 不需要滚动时保持居中
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                containerWidthPx = size.width
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            onTextLayout = { textLayoutResult ->
                textWidthPx = with(density) { 
                    textLayoutResult.size.width 
                }
            },
            modifier = Modifier
                .graphicsLayer {
                    translationX = if (needsScrolling) offset else 0f
                }
                .fillMaxWidth()
                .padding(horizontal = 30.dp), // 减小间距到30dp以显示更多内容
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LyricsMiniDisplay(
    lyrics: List<LyricsLine>,
    currentLyricIndex: Int?,
    colors: JellyTunesColors,
    modifier: Modifier = Modifier
) {
    val displayLyrics = remember(lyrics, currentLyricIndex) {
        if (lyrics.isEmpty()) {
            listOf("暂无歌词", "")
        } else {
            val currentIndex = currentLyricIndex ?: 0
            val firstLine = lyrics.getOrNull(currentIndex)?.value?.ifEmpty { "♪" } ?: "♪"
            val secondLine = lyrics.getOrNull(currentIndex + 1)?.value?.ifEmpty { "♪" } ?: ""
            listOf(firstLine, secondLine)
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Current lyric line (highlighted) - allow wrapping for long lyrics
        Text(
            text = displayLyrics[0],
            style = JellyTunesTypography.trackTitle.copy(
                color = colors.primary,
                fontSize = JellyTunesTypography.trackTitle.fontSize * 0.7f
            ),
            maxLines = 3,  // 允许最多3行显示
            overflow = TextOverflow.Visible,  // 显示完整内容而不省略
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Next lyric line (muted) - allow wrapping for long lyrics
        Text(
            text = displayLyrics[1],
            style = JellyTunesTypography.artistName.copy(
                color = colors.textMuted,
                fontSize = JellyTunesTypography.artistName.fontSize * 0.8f
            ),
            maxLines = 2,  // 允许最多2行显示
            overflow = TextOverflow.Visible,  // 显示完整内容而不省略
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
