package com.github.easymobile.ui.editor

import android.content.Context
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.github.easymobile.R

class CodeEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyleAttr) {
    
    init {
        // Configure the EditText for code editing
        setupCodeEditor()
    }
    
    private fun setupCodeEditor() {
        // Set monospace font for code
        setTypeface(Typeface.MONOSPACE)
        
        // Set text size
        textSize = 14f // 14sp
        
        // Enable scrolling
        movementMethod = ScrollingMovementMethod()
        
        // Enable text selection for easy copy/paste
        setTextIsSelectable(true)
        setLongClickable(true)
        
        // Set minimum lines for better UX
        minLines = 20
        
        // Handle keyboard actions
        setOnEditorActionListener { _, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT -> {
                    // Handle done/next actions
                    true
                }
                else -> false
            }
        }
        
        // Handle key events for better code editing experience
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                handleKeyEvent(keyCode, event)
            } else {
                false
            }
        }
    }
    
    private fun handleKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_TAB -> {
                // Insert tab character
                val start = selectionStart
                val end = selectionEnd
                if (start != end) {
                    // Tab selected text
                    val selectedText = text.subSequence(start, end).toString()
                    val indentedText = selectedText.replace("\n", "\n    ")
                    text.replace(start, end, indentedText)
                    setSelection(start + indentedText.length)
                } else {
                    // Insert tab
                    text.insert(start, "    ") // 4 spaces instead of tab
                }
                return true
            }
            
            KeyEvent.KEYCODE_ENTER -> {
                // Auto-indent
                val start = selectionStart
                val beforeCursor = text.subSequence(0, start).toString()
                val lineStart = beforeCursor.lastIndexOf('\n') + 1
                val currentIndent = beforeCursor.substring(lineStart, start)
                
                val leadingSpaces = currentIndent.length - currentIndent.trimStart().length
                val newLine = "\n${" ".repeat(leadingSpaces)}"
                
                text.insert(start, newLine)
                setSelection(start + newLine.length)
                return true
            }
        }
        return false
    }
    
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        // Update line numbers or other UI elements if needed
    }
    
    fun getCurrentLine(): String {
        val selectionStart = selectionStart
        val textStr = text.toString()
        val lineStart = textStr.lastIndexOf('\n', selectionStart - 1) + 1
        val lineEnd = textStr.indexOf('\n', selectionStart)
        return if (lineEnd != -1) {
            textStr.substring(lineStart, lineEnd)
        } else {
            textStr.substring(lineStart)
        }
    }
    
    fun getCurrentLineNumber(): Int {
        return text.toString().substring(0, selectionStart).count { it == '\n' } + 1
    }
    
    fun getTotalLines(): Int {
        return text.toString().count { it == '\n' } + 1
    }
    
    fun insertAtCursor(text: String) {
        val start = selectionStart
        val end = selectionEnd
        this.text?.replace(start, end, text)
        setSelection(start + text.length)
    }
    
    fun selectCurrentWord(): Boolean {
        val start = selectionStart
        val end = selectionEnd
        
        if (start != end) return true // Already selected
        
        val textStr = text.toString()
        val beforeCursor = textStr.substring(0, start)
        val afterCursor = textStr.substring(start)
        
        // Find word boundaries
        val wordStart = findWordStart(beforeCursor)
        val wordEnd = findWordEnd(afterCursor)
        
        if (wordStart < start && wordEnd > 0) {
            setSelection(wordStart, start + wordEnd)
            return true
        }
        return false
    }
    
    private fun findWordStart(text: String): Int {
        var i = text.length - 1
        while (i >= 0 && Character.isLetterOrDigit(text[i])) {
            i--
        }
        return i + 1
    }
    
    private fun findWordEnd(text: String): Int {
        var i = 0
        while (i < text.length && Character.isLetterOrDigit(text[i])) {
            i++
        }
        return i
    }
}