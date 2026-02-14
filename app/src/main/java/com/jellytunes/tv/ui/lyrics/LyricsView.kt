package com.jellytunes.tv.ui.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jellytunes.tv.data.metadata.Lyrics

@Composable
fun LyricsView(
    lyrics: Lyrics?,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    if (lyrics == null || lyrics.lines.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pure Music",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }
    
    val listState = rememberLazyListState()
    
    // Find current line index
    val currentLineIndex = remember(currentPositionMs, lyrics) {
        lyrics.lines.indexOfLast { it.timeMs <= currentPositionMs }
            .coerceAtLeast(0)
    }
    
    // Auto-scroll to current line
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            listState.animateScrollToItem(
                index = currentLineIndex,
                scrollOffset = -100
            )
        }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val isCurrentLine = index == currentLineIndex
            val isPastLine = index < currentLineIndex
            
            val textColor by animateColorAsState(
                targetValue = when {
                    isCurrentLine -> MaterialTheme.colorScheme.primary
                    isPastLine -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                animationSpec = tween(300),
                label = "lyric_color"
            )
            
            Text(
                text = line.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = if (isCurrentLine) 20.sp else 16.sp,
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun LyricsViewCompact(
    lyrics: Lyrics?,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    if (lyrics == null || lyrics.lines.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pure Music",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }
    
    val currentLineIndex = remember(currentPositionMs, lyrics) {
        lyrics.lines.indexOfLast { it.timeMs <= currentPositionMs }
            .coerceAtLeast(0)
    }
    
    val currentLine = lyrics.lines.getOrNull(currentLineIndex)
    val nextLine = lyrics.lines.getOrNull(currentLineIndex + 1)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentLine != null) {
            Text(
                text = currentLine.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (nextLine != null) {
            Text(
                text = nextLine.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
