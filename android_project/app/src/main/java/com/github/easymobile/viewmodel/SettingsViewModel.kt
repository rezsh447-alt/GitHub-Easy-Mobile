package com.github.easymobile.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    
    private var sharedPreferences: SharedPreferences? = null
    private val TOKEN_KEY = "github_personal_token"
    private val CACHE_KEY = "cache_data"
    
    private val _tokenSaved = MutableLiveData<Boolean>()
    val tokenSaved: LiveData<Boolean> = _tokenSaved
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _cacheCleared = MutableLiveData<Boolean>()
    val cacheCleared: LiveData<Boolean> = _cacheCleared
    
    fun setSharedPreferences(sharedPrefs: SharedPreferences) {
        sharedPreferences = sharedPrefs
    }
    
    fun getSavedToken(): String {
        return sharedPreferences?.getString(TOKEN_KEY, "") ?: ""
    }
    
    fun saveToken(token: String) {
        if (token.isEmpty()) {
            _error.value = "Token cannot be empty"
            return
        }
        
        if (!isValidTokenFormat(token)) {
            _error.value = "Invalid token format. Token should start with 'ghp_' or 'github_pat_'"
            return
        }
        
        try {
            sharedPreferences?.edit()
                ?.putString(TOKEN_KEY, token)
                ?.apply()
            
            _tokenSaved.value = true
        } catch (e: Exception) {
            _error.value = "Failed to save token: ${e.message}"
        }
    }
    
    fun clearToken() {
        sharedPreferences?.edit()
            ?.remove(TOKEN_KEY)
            ?.apply()
    }
    
    fun clearCache() {
        try {
            // Clear repository cache
            sharedPreferences?.edit()
                ?.remove("${CACHE_KEY}_repositories")
                ?.remove("${CACHE_KEY}_file_contents")
                ?.apply()
            
            _cacheCleared.value = true
        } catch (e: Exception) {
            _error.value = "Failed to clear cache: ${e.message}"
        }
    }
    
    private fun isValidTokenFormat(token: String): Boolean {
        // GitHub personal access tokens start with "ghp_"
        // GitHub App tokens start with "github_pat_"
        return token.startsWith("ghp_") || token.startsWith("github_pat_") ||
               token.length >= 40 // General length check for various token types
    }
    
    fun resetTokenSavedState() {
        _tokenSaved.value = false
    }
    
    fun resetCacheClearedState() {
        _cacheCleared.value = false
    }
    
    fun clearError() {
        _error.value = null
    }
}