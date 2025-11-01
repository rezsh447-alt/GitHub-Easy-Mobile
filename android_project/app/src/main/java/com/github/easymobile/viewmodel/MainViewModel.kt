package com.github.easymobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.easymobile.model.Repository
import com.github.easymobile.repository.GitHubRepository
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel : ViewModel() {
    
    private val repository = GitHubRepository()
    
    private val _repositories = MutableLiveData<List<Repository>>()
    val repositories: LiveData<List<Repository>> = _repositories
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _githubToken = MutableLiveData<String?>()
    val githubToken: LiveData<String?> = _githubToken
    
    init {
        checkToken()
    }
    
    fun checkToken() {
        val token = repository.getToken()
        _githubToken.value = token
        
        if (token.isNotEmpty()) {
            loadRepositories()
        }
    }
    
    fun loadRepositories() {
        val token = _githubToken.value ?: return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val repos = repository.getUserRepositories(token)
                _repositories.value = repos.sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is UnauthorizedException -> "Invalid GitHub token. Please check your settings."
                    else -> "Failed to load repositories: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun search(query: String) {
        val currentRepos = _repositories.value ?: return
        val filtered = if (query.isEmpty()) {
            currentRepos
        } else {
            currentRepos.filter { 
                it.name.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        _repositories.value = filtered
    }
}

class UnauthorizedException : Exception("GitHub token is invalid or expired")