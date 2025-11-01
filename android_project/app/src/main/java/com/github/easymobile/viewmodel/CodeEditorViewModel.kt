package com.github.easymobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.easymobile.model.Repository
import com.github.easymobile.repository.GitHubRepository
import com.github.easymobile.repository.FileContent
import com.github.easymobile.repository.FileCreateResponse
import kotlinx.coroutines.launch
import com.github.easymobile.repository.UnauthorizedException

class CodeEditorViewModel : ViewModel() {
    
    private val repository = GitHubRepository()
    private var currentRepository: Repository? = null
    private var currentFilePath = ""
    private var currentFileSha = ""
    private var currentContent = ""
    private var currentToken = ""
    
    private val _fileContent = MutableLiveData<String>()
    val fileContent: LiveData<String> = _fileContent
    
    private val _lineCount = MutableLiveData<Int>()
    val lineCount: LiveData<Int> = _lineCount
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean> = _saved
    
    private val _isModified = MutableLiveData<Boolean>()
    val isModified: LiveData<Boolean> = _isModified
    
    fun setRepository(repo: Repository) {
        currentRepository = repo
    }
    
    fun setFileInfo(path: String, sha: String) {
        currentFilePath = path
        currentFileSha = sha
    }
    
    fun setContent(content: String) {
        currentContent = content
        updateLineCount()
    }
    
    fun setModified(modified: Boolean) {
        _isModified.value = modified
    }
    
    fun loadFileContent() {
        val repo = currentRepository ?: return
        val token = getToken()
        val path = currentFilePath
        
        if (token.isEmpty() || path.isEmpty()) return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response = repository.getFileContent(token, repo.owner, repo.name, path)
                if (response.encoding == "base64") {
                    val decodedContent = android.util.Base64.decode(response.content, android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
                    _fileContent.value = decodedContent
                    currentContent = decodedContent
                    _isModified.value = false
                } else {
                    throw Exception("Unsupported encoding: ${response.encoding}")
                }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is UnauthorizedException -> "Invalid GitHub token. Please check your settings."
                    else -> "Failed to load file: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveFile() {
        val repo = currentRepository ?: return
        val token = getToken()
        val path = currentFilePath
        val content = currentContent
        
        if (token.isEmpty() || path.isEmpty()) {
            _error.value = "Missing repository info or token"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response: FileCreateResponse
                if (currentFileSha.isEmpty()) {
                    // Create new file
                    response = repository.createFile(
                        token, repo.owner, repo.name, path, 
                        content, "Update $path via GitHub Easy Mobile"
                    )
                } else {
                    // Update existing file
                    response = repository.updateFile(
                        token, repo.owner, repo.name, path, 
                        content, "Update $path via GitHub Easy Mobile",
                        currentFileSha
                    )
                }
                
                // Update file SHA for future updates
                currentFileSha = response.content.sha
                _saved.value = true
                
                // Reset saved state after a delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    _saved.value = false
                }
                
            } catch (e: Exception) {
                _error.value = when (e) {
                    is UnauthorizedException -> "Invalid GitHub token. Please check your settings."
                    else -> "Failed to save file: ${e.message}"
                }
                _saved.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getToken(): String {
        // TODO: Get from SharedPreferences
        return currentToken
    }
    
    fun setToken(token: String) {
        currentToken = token
    }
    
    private fun updateLineCount() {
        val lines = currentContent.count { it == '\n' } + 1
        _lineCount.value = lines
    }
    
    fun clearError() {
        _error.value = null
    }
}