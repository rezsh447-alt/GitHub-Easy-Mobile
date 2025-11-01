package com.github.easymobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.github.easymobile.databinding.ActivityMainBinding
import com.github.easymobile.ui.repo.RepoBrowserActivity
import com.github.easymobile.ui.settings.SettingsActivity
import com.github.easymobile.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: RepositoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        observeData()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }
    
    private fun setupUI() {
        // Setup toolbar
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Setup RecyclerView
        adapter = RepositoryAdapter { repository ->
            val intent = Intent(this, RepoBrowserActivity::class.java)
            intent.putExtra("repository", repository)
            startActivity(intent)
        }
        
        binding.repoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        
        // Setup search
        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            viewModel.search(binding.searchEditText.text.toString())
            true
        }
        
        // Setup retry button
        binding.retryButton.setOnClickListener {
            viewModel.loadRepositories()
        }
        
        // Setup window insets for dark theme
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBarsVisible = insets.isVisible(WindowInsetsCompat.Type.systemBars())
            insets
        }
    }
    
    private fun observeData() {
        viewModel.repositories.observe(this) { repositories ->
            adapter.updateRepositories(repositories)
            
            if (repositories.isEmpty()) {
                binding.emptyStateLayout.visibility = android.view.View.VISIBLE
                binding.errorStateLayout.visibility = android.view.View.GONE
            } else {
                binding.emptyStateLayout.visibility = android.view.View.GONE
                binding.errorStateLayout.visibility = android.view.View.GONE
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.errorStateLayout.visibility = android.view.View.VISIBLE
                binding.emptyStateLayout.visibility = android.view.View.GONE
            }
        }
        
        viewModel.githubToken.observe(this) { token ->
            if (token.isNullOrEmpty()) {
                binding.emptyStateLayout.visibility = android.view.View.VISIBLE
                binding.emptyStateLayout.findViewById<TextView>(R.id.emptyText)?.text = 
                    "Please set your GitHub token in Settings"
            } else {
                viewModel.loadRepositories()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh repositories when returning to this activity
        viewModel.checkToken()
    }
}