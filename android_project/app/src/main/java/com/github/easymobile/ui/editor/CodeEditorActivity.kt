package com.github.easymobile.ui.editor

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.easymobile.databinding.ActivityCodeEditorBinding
import com.github.easymobile.model.Repository
import com.github.easymobile.viewmodel.CodeEditorViewModel
import android.text.method.ScrollingMovementMethod
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import android.text.SpannableString
import java.util.regex.Pattern

class CodeEditorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCodeEditorBinding
    private lateinit var viewModel: CodeEditorViewModel
    private lateinit var repository: Repository
    private var filePath = ""
    private var fileSha = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityCodeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupIntentData()
        setupViewModel()
        setupUI()
        observeData()
    }
    
    private fun setupIntentData() {
        repository = intent.getSerializableExtra("repository") as Repository
        filePath = intent.getStringExtra("file_path") ?: ""
        fileSha = intent.getStringExtra("file_sha") ?: ""
        
        binding.toolbar.title = filePath.substringAfterLast('/')
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CodeEditorViewModel::class.java]
        viewModel.setRepository(repository)
        viewModel.setFileInfo(filePath, fileSha)
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Setup code editor
        setupCodeEditor()
        
        // Setup action bar
        binding.saveAndCloseButton.setOnClickListener {
            saveAndClose()
        }
        
        binding.closeButton.setOnClickListener {
            onBackPressed()
        }
        
        // Load file content
        viewModel.loadFileContent()
    }
    
    private fun setupCodeEditor() {
        binding.codeEditor.apply {
            setBackgroundColor(getColorFromResources(R.color.surface_1))
            setTextColor(getColorFromResources(R.color.text_primary))
            setTextSize(14f) // 14sp
            setTypeface(createTypeface())
            setMovementMethod(ScrollingMovementMethod())
            
            // Enable text selection (this is the key feature!)
            setTextIsSelectable(true)
            setLongClickable(true)
            setOnLongClickListener {
                // Enable selection mode for long press
                showSelectionHandles()
                true
            }
            
            // Handle text changes for modified indicator
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.modifiedIndicator.visibility = android.view.View.VISIBLE
                    viewModel.setModified(true)
                    viewModel.setContent(s.toString())
                }
                
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        
        // Load syntax highlighting based on file extension
        updateSyntaxHighlighting(filePath)
    }
    
    private fun updateSyntaxHighlighting(filePath: String) {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        when (extension) {
            "kt", "java" -> applyKotlinSyntaxHighlighting()
            "js", "ts" -> applyJavaScriptSyntaxHighlighting()
            "py" -> applyPythonSyntaxHighlighting()
            "xml", "html" -> applyXMLSyntaxHighlighting()
            "css" -> applyCSSSyntaxHighlighting()
            else -> applyPlainTextHighlighting()
        }
    }
    
    private fun applyKotlinSyntaxHighlighting() {
        // Keywords
        val keywords = Pattern.compile("\\b(class|fun|val|var|if|else|when|for|while|return|import|package|public|private|protected|override|abstract|interface|enum|object|data|sealed|suspend|coroutine)\\b")
        applySyntaxHighlighting(keywords, R.color.syntax_keyword)
        
        // Strings
        val strings = Pattern.compile("\".*?\"|'.*?'")
        applySyntaxHighlighting(strings, R.color.syntax_string)
        
        // Comments
        val comments = Pattern.compile("//.*?\$|//.*")
        applySyntaxHighlighting(comments, R.color.syntax_comment, Pattern.MULTILINE)
        
        // Numbers
        val numbers = Pattern.compile("\\b\\d+(?:\\.\\d+)?[fFdDlL]?\\b")
        applySyntaxHighlighting(numbers, R.color.syntax_number)
        
        // Functions
        val functions = Pattern.compile("\\b\\w+(?=\\()")
        applySyntaxHighlighting(functions, R.color.syntax_function)
    }
    
    private fun applyJavaScriptSyntaxHighlighting() {
        // Keywords
        val keywords = Pattern.compile("\\b(function|var|let|const|if|else|for|while|return|import|export|class|extends|super|this|new|try|catch|throw|async|await)\\b")
        applySyntaxHighlighting(keywords, R.color.syntax_keyword)
        
        // Strings
        val strings = Pattern.compile("\".*?\"|'.*?'|`.*?`")
        applySyntaxHighlighting(strings, R.color.syntax_string)
        
        // Comments
        val comments = Pattern.compile("//.*?\$|//.*|/\\*.*?\\*/")
        applySyntaxHighlighting(comments, R.color.syntax_comment, Pattern.MULTILINE)
        
        // Numbers
        val numbers = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b")
        applySyntaxHighlighting(numbers, R.color.syntax_number)
    }
    
    private fun applyPythonSyntaxHighlighting() {
        // Keywords
        val keywords = Pattern.compile("\\b(def|class|if|elif|else|for|while|return|import|from|as|try|except|finally|with|lambda|yield|global|nonlocal|True|False|None|and|or|not|in|is)\\b")
        applySyntaxHighlighting(keywords, R.color.syntax_keyword)
        
        // Strings
        val strings = Pattern.compile("\".*?\"|'.*?'|\"\"\".*?\"\"\"|'''.*?'''")
        applySyntaxHighlighting(strings, R.color.syntax_string)
        
        // Comments
        val comments = Pattern.compile("#.*?\$")
        applySyntaxHighlighting(comments, R.color.syntax_comment, Pattern.MULTILINE)
        
        // Numbers
        val numbers = Pattern.compile("\\b\\d+(?:\\.\\d+)?[jJ]?\\b")
        applySyntaxHighlighting(numbers, R.color.syntax_number)
    }
    
    private fun applyXMLSyntaxHighlighting() {
        // Tags
        val tags = Pattern.compile("</?[^>]+/?>")
        applySyntaxHighlighting(tags, R.color.syntax_keyword)
        
        // Attributes
        val attributes = Pattern.compile("\\s+[a-zA-Z-]+=")
        applySyntaxHighlighting(attributes, R.color.syntax_function)
        
        // Strings in attributes
        val strings = Pattern.compile("\".*?\"|'.*?'")
        applySyntaxHighlighting(strings, R.color.syntax_string)
        
        // Comments
        val comments = Pattern.compile("<!--.*?-->")
        applySyntaxHighlighting(comments, R.color.syntax_comment, Pattern.MULTILINE)
    }
    
    private fun applyCSSSyntaxHighlighting() {
        // Properties
        val properties = Pattern.compile("^\\s*[a-zA-Z-]+(?=\\s*:)")
        applySyntaxHighlighting(properties, R.color.syntax_function)
        
        // Values
        val values = Pattern.compile(":(?!//)[^;]+;")
        applySyntaxHighlighting(values, R.color.syntax_string)
        
        // Selectors
        val selectors = Pattern.compile("^[.#]?[a-zA-Z][^{]*")
        applySyntaxHighlighting(selectors, R.color.syntax_keyword)
        
        // Comments
        val comments = Pattern.compile("/\\*.*?\\*/")
        applySyntaxHighlighting(comments, R.color.syntax_comment, Pattern.MULTILINE)
    }
    
    private fun applyPlainTextHighlighting() {
        // No syntax highlighting for plain text
    }
    
    private fun applySyntaxHighlighting(pattern: Pattern, colorResId: Int, flags: Int = 0) {
        val content = binding.codeEditor.text
        val matcher = pattern.matcher(content)
        
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            
            val color = getColorFromResources(colorResId)
            val span = ForegroundColorSpan(color)
            content.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    
    private fun getColorFromResources(colorRes: Int): Int {
        return resources.getColor(colorRes, theme)
    }
    
    private fun createTypeface(): android.graphics.Typeface {
        return android.graphics.Typeface.MONOSPACE
    }
    
    private fun showSelectionHandles() {
        // This will be handled by the system for text selection
        // The key is setTextIsSelectable(true) and setLongClickable(true)
    }
    
    private fun observeData() {
        viewModel.fileContent.observe(this) { content ->
            binding.codeEditor.setText(content)
            binding.modifiedIndicator.visibility = android.view.View.GONE
        }
        
        viewModel.lineCount.observe(this) { count ->
            binding.lineCountText.text = "Lines: $count"
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.saved.observe(this) { isSaved ->
            if (isSaved) {
                showSaveIndicator()
                binding.modifiedIndicator.visibility = android.view.View.GONE
                viewModel.setModified(false)
            }
        }
    }
    
    private fun saveAndClose() {
        if (viewModel.isModified.value == true) {
            viewModel.saveFile()
            // Close after save completes
            viewModel.saved.observe(this) { isSaved ->
                if (isSaved) {
                    finish()
                }
            }
        } else {
            finish()
        }
    }
    
    private fun showSaveIndicator() {
        binding.saveIndicator.visibility = android.view.View.VISIBLE
        binding.saveIndicator.postDelayed({
            binding.saveIndicator.visibility = android.view.View.GONE
        }, 2000)
    }
    
    override fun onBackPressed() {
        if (viewModel.isModified.value == true) {
            // Show unsaved changes dialog
            showUnsavedChangesDialog()
        } else {
            finish()
        }
    }
    
    private fun showUnsavedChangesDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Save before closing?")
            .setPositiveButton("Save & Close") { _, _ ->
                viewModel.saveFile()
                viewModel.saved.observe(this) { isSaved ->
                    if (isSaved) finish()
                }
            }
            .setNegativeButton("Close without saving") { _, _ ->
                finish()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                if (viewModel.isModified.value == true) {
                    viewModel.saveFile()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}