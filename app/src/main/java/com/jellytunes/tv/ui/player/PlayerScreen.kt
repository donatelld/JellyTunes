package com.jellytunes.tv.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jellytunes.tv.ui.theme.JellyTunesColors
import com.jellytunes.tv.ui.theme.JellyTunesTypography
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
                        Key.DirectionUp, Key.DirectionDown -> {
                            onThemeChange()
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
                            .padding(top = 32.dp, bottom = 32.dp, start = 48.dp)
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

            // Right side - Track info and controls (45% width)
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .padding(end = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    playerState.currentTrack?.let { track ->
                        // Track title
                        Text(
                            text = track.title,
                            style = JellyTunesTypography.trackTitle.copy(
                                color = colors.textPrimary,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(0f, 4f),
                                    blurRadius = 8f
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(48.dp))

                    // Play/Pause button with glow effect
                    PlayPauseButton(
                        isPlaying = playerState.isPlaying,
                        colors = colors,
                        onClick = onPlayPauseToggle
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Progress bar
                    ProgressSection(
                        currentPositionMs = playerState.currentPositionMs,
                        durationMs = playerState.durationMs,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation hint
                    Text(
                        text = "< >  TRACK  ·  ↑↓  THEME",
                        style = JellyTunesTypography.hint.copy(
                            color = colors.textMuted
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Theme indicator - top right
        Text(
            text = colors.name.uppercase(),
            style = JellyTunesTypography.brand.copy(
                color = colors.primary
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        )

        // Brand watermark - bottom right
        Text(
            text = "JELLYTUNES",
            style = JellyTunesTypography.brand.copy(
                color = colors.textMuted
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
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
            .aspectRatio(1f)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = colors.primary.copy(alpha = 0.3f),
                spotColor = colors.primary.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(16.dp)),
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
        targetValue = if (isPlaying) 0.6f else 0.3f,
        animationSpec = tween(300),
        label = "glow"
    )

    val animatedPrimary by animateColorAsState(
        targetValue = colors.primary,
        animationSpec = tween(500),
        label = "primary"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .drawBehind {
                // Glow effect
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = size.minDimension
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.primary, colors.primaryLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(36.dp),
                tint = colors.textPrimary
            )
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
            .padding(horizontal = 24.dp)
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
