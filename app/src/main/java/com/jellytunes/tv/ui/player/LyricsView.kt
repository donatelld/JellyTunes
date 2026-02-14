package com.jellytunes.tv.ui.player

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jellytunes.tv.R
import com.jellytunes.tv.data.model.LyricsLine

class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var lyricsLines: List<LyricsLine> = emptyList()
    private var currentLineIndex: Int? = null
    private val textViewCache = mutableMapOf<Int, TextView>()

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun setLyrics(lines: List<LyricsLine>) {
        lyricsLines = lines
        currentLineIndex = null
        removeAllViews()
        textViewCache.clear()
        
        if (lines.isEmpty()) {
            addEmptyMessage()
        } else {
            lines.forEachIndexed { index, line ->
                val textView = createLyricTextView(line.value, index)
                textViewCache[index] = textView
                addView(textView)
            }
        }
    }

    fun setCurrentLine(index: Int?) {
        if (currentLineIndex == index) return
        
        // Reset previous highlighted line
        currentLineIndex?.let { prevIndex ->
            textViewCache[prevIndex]?.setTextColor(Color.WHITE)
        }
        
        currentLineIndex = index
        
        // Highlight current line
        index?.let { currentIndex ->
            textViewCache[currentIndex]?.setTextColor(
                ContextCompat.getColor(context, R.color.lyric_highlight)
            )
            
            // Scroll to center the current line
            scrollToCenter(currentIndex)
        }
    }

    private fun createLyricTextView(text: String, index: Int): TextView {
        return TextView(context).apply {
            this.text = text.ifEmpty { "♪" } // Show musical note for empty lines
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            
            // Set focusable for navigation
            isFocusable = true
            isFocusableInTouchMode = true
            
            setOnClickListener {
                // Handle click to seek to this lyric time
                lyricsLines.getOrNull(index)?.let { line ->
                    // Notify parent to seek to this time
                    (parent as? LyricsInteractionListener)?.onLyricClicked(line.start)
                }
            }
        }
    }

    private fun addEmptyMessage() {
        val textView = TextView(context).apply {
            text = "暂无歌词"
            textSize = 16f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }
        addView(textView)
    }

    private fun scrollToCenter(index: Int) {
        // Calculate scroll position to center the current line
        val targetView = textViewCache[index] ?: return
        val targetTop = targetView.top
        val targetHeight = targetView.height
        val containerHeight = height
        val scrollY = targetTop - (containerHeight / 2) + (targetHeight / 2)
        
        // Simple scroll without animation for now
        scrollTo(0, scrollY.coerceAtLeast(0))
    }

    interface LyricsInteractionListener {
        fun onLyricClicked(timestamp: Long)
    }
}