package com.github.easymobile.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.easymobile.databinding.ActivitySettingsBinding
import com.github.easymobile.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupSharedPreferences()
        setupViewModel()
        setupUI()
        observeData()
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("github_easy_mobile", MODE_PRIVATE)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        viewModel.setSharedPreferences(sharedPreferences)
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Setup token input
        binding.tokenEditText.setText(viewModel.getSavedToken())
        
        // Setup save button
        binding.saveTokenButton.setOnClickListener {
            val token = binding.tokenEditText.text?.toString()?.trim() ?: ""
            
            if (token.isEmpty()) {
                binding.tokenInputLayout.error = "Token cannot be empty"
                return@setOnClickListener
            }
            
            if (!token.startsWith("ghp_") && !token.startsWith("github_pat_")) {
                binding.tokenInputLayout.error = "Invalid token format"
                return@setOnClickListener
            }
            
            viewModel.saveToken(token)
        }
        
        // Setup actions
        binding.clearCacheLayout.setOnClickListener {
            showClearCacheDialog()
        }
        
        binding.aboutLayout.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun observeData() {
        viewModel.tokenSaved.observe(this) { isSaved ->
            if (isSaved) {
                binding.tokenInputLayout.error = null
                Toast.makeText(this, "Token saved successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetTokenSavedState()
            }
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        viewModel.cacheCleared.observe(this) { isCleared ->
            if (isCleared) {
                Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetCacheClearedState()
            }
        }
    }
    
    private fun showClearCacheDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will clear all cached data including repository lists and file contents. Are you sure?")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearCache()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About GitHub Easy Mobile")
            .setMessage("A lightweight GitHub client for mobile development.\n\n" +
                        "Features:\n" +
                        "• Browse repositories\n" +
                        "• Edit code files with syntax highlighting\n" +
                        "• Commit and push changes\n" +
                        "• Fast and user-friendly interface\n\n" +
                        "Version: 1.0.0")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}