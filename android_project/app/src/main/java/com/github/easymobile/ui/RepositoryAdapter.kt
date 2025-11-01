package com.github.easymobile.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.easymobile.databinding.ItemRepositoryBinding
import com.github.easymobile.model.Repository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RepositoryAdapter(
    private val onItemClick: (Repository) -> Unit
) : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {
    
    private var repositories = mutableListOf<Repository>()
    
    fun updateRepositories(newRepositories: List<Repository>) {
        repositories.clear()
        repositories.addAll(newRepositories)
        notifyDataSetChanged()
    }
    
    fun filterRepositories(query: String) {
        val filtered = if (query.isEmpty()) {
            repositories
        } else {
            repositories.filter { 
                it.name.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val binding = ItemRepositoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepositoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        holder.bind(repositories[position], onItemClick)
    }
    
    override fun getItemCount(): Int = repositories.size
    
    class RepositoryViewHolder(
        private val binding: ItemRepositoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(repository: Repository, onItemClick: (Repository) -> Unit) {
            binding.repoName.text = repository.name
            binding.repoDescription.text = repository.description
            binding.repoDescription.visibility = if (repository.description.isNullOrEmpty()) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
            
            // Language chip
            if (!repository.language.isNullOrEmpty()) {
                binding.languageChip.apply {
                    visibility = android.view.View.VISIBLE
                    text = repository.language
                }
            } else {
                binding.languageChip.visibility = android.view.View.GONE
            }
            
            // Stats
            binding.repoStars.text = repository.starsCount.toString()
            binding.repoForks.text = repository.forksCount.toString()
            binding.repoUpdated.text = formatDate(repository.updatedAt)
            
            // Click listener
            binding.root.setOnClickListener { onItemClick(repository) }
        }
        
        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Updated ${outputFormat.format(date ?: Date())}"
            } catch (e: Exception) {
                "Updated ${dateString.take(10)}"
            }
        }
    }
}