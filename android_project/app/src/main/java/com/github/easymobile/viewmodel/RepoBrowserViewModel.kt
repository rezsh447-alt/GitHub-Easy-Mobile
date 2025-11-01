package com.github.easymobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.easymobile.model.Repository
import com.github.easymobile.repository.GitHubRepository
import com.github.easymobile.repository.FileItem
import kotlinx.coroutines.launch
import com.github.easymobile.repository.UnauthorizedException

class RepoBrowserViewModel : ViewModel() {
    
    private val repository = GitHubRepository()
    private var currentRepository: Repository? = null
    private var currentToken: String = ""
    
    private val _fileItems = MutableLiveData<List<FileItem>>()
    val fileItems: LiveData<List<FileItem>> = _fileItems
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun setRepository(repo: Repository) {
        currentRepository = repo
        // Get token from repository - this should come from settings
        // For now, we'll need to get it from SharedPreferences
    }
    
    fun loadContents(path: String) {
        val repo = currentRepository ?: return
        val token = getToken() ?: return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val contents = repository.getRepositoryContents(token, repo.owner, repo.name, path)
                _fileItems.value = contents.sortedWith(
                    compareByDescending<FileItem> { it.type == "dir" }
                        .thenBy { it.name.lowercase() }
                )
            } catch (e: Exception) {
                _error.value = when (e) {
                    is UnauthorizedException -> "Invalid GitHub token. Please check your settings."
                    is NoNetworkException -> "No internet connection. Please check your network."
                    else -> "Failed to load contents: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getToken(): String? {
        // TODO: Get token from SharedPreferences
        // This should be implemented properly with context
        return currentToken.ifEmpty { null }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun setToken(token: String) {
        currentToken = token
    }
}

class NoNetworkException : Exception("No network connection available")