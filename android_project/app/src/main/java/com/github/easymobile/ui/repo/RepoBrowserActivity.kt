package com.github.easymobile.ui.repo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.easymobile.R
import com.github.easymobile.databinding.ActivityRepoBrowserBinding
import com.github.easymobile.model.Repository
import com.github.easymobile.ui.editor.CodeEditorActivity
import com.github.easymobile.viewmodel.RepoBrowserViewModel

class RepoBrowserActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRepoBrowserBinding
    private lateinit var viewModel: RepoBrowserViewModel
    private lateinit var adapter: FileAdapter
    private lateinit var repository: Repository
    private var currentPath = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRepoBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRepository()
        setupViewModel()
        setupUI()
        observeData()
    }
    
    private fun setupRepository() {
        repository = intent.getSerializableExtra("repository") as Repository
        binding.toolbar.title = repository.name
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[RepoBrowserViewModel::class.java]
        viewModel.setRepository(repository)
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Setup RecyclerView
        adapter = FileAdapter { fileItem ->
            if (fileItem.type == "dir") {
                // Navigate into folder
                currentPath = if (currentPath.isEmpty()) {
                    fileItem.name
                } else {
                    "$currentPath/${fileItem.name}"
                }
                viewModel.loadContents(currentPath)
                updateBreadcrumb()
            } else {
                // Open file
                val intent = Intent(this, CodeEditorActivity::class.java)
                intent.putExtra("repository", repository)
                intent.putExtra("file_path", "$currentPath/${fileItem.name}")
                intent.putExtra("file_sha", fileItem.sha)
                startActivity(intent)
            }
        }
        
        binding.fileRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RepoBrowserActivity)
            adapter = this@RepoBrowserActivity.adapter
        }
        
        // Setup FABs
        binding.newFileFab.setOnClickListener {
            showNewFileDialog()
        }
        
        binding.newFolderFab.setOnClickListener {
            showNewFolderDialog()
        }
        
        binding.deleteFab.setOnClickListener {
            // TODO: Show delete confirmation dialog
            Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Setup commit button
        binding.commitPushButton.setOnClickListener {
            // TODO: Show commit dialog
            Toast.makeText(this, "Commit functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Load initial contents
        viewModel.loadContents("")
    }
    
    private fun observeData() {
        viewModel.fileItems.observe(this) { fileItems ->
            adapter.updateFiles(fileItems)
            
            binding.emptyStateLayout.visibility = if (fileItems.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun updateBreadcrumb() {
        binding.breadcrumbGroup.removeAllViews()
        
        // Add root
        val rootChip = createChip("Root") {
            currentPath = ""
            viewModel.loadContents("")
            updateBreadcrumb()
        }
        binding.breadcrumbGroup.addView(rootChip)
        
        // Add path segments
        if (currentPath.isNotEmpty()) {
            val segments = currentPath.split("/")
            for (segment in segments) {
                binding.breadcrumbGroup.addView(createSeparatorChip())
                val chip = createChip(segment) {
                    val newPath = segments.take(segments.indexOf(segment) + 1).joinToString("/")
                    currentPath = newPath
                    viewModel.loadContents(newPath)
                    updateBreadcrumb()
                }
                binding.breadcrumbGroup.addView(chip)
            }
        }
    }
    
    private fun createChip(text: String, onClick: () -> Unit): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(this).apply {
            this.text = text
            isCheckable = false
            isClickable = true
            setOnClickListener { onClick() }
        }
    }
    
    private fun createSeparatorChip(): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(this).apply {
            text = "/"
            isEnabled = false
            visibility = com.google.android.material.chip.Chip.INVISIBLE
        }
    }
    
    private fun showNewFileDialog() {
        // TODO: Implement new file dialog
        Toast.makeText(this, "New file dialog coming soon", Toast.LENGTH_SHORT).show()
    }
    
    private fun showNewFolderDialog() {
        // TODO: Implement new folder dialog
        Toast.makeText(this, "New folder dialog coming soon", Toast.LENGTH_SHORT).show()
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