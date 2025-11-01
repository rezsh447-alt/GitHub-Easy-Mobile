package com.github.easymobile.ui.repo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.easymobile.databinding.ItemFileBinding
import com.github.easymobile.repository.FileItem
import android.content.Context
import android.content.res.Resources

class FileAdapter(
    private val onItemClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {
    
    private var fileItems = mutableListOf<FileItem>()
    
    fun updateFiles(newFiles: List<FileItem>) {
        fileItems.clear()
        fileItems.addAll(newFiles)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileItems[position], onItemClick)
    }
    
    override fun getItemCount(): Int = fileItems.size
    
    class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(fileItem: FileItem, onItemClick: (FileItem) -> Unit) {
            binding.fileName.text = fileItem.name
            
            // Set icon based on file type
            when (fileItem.type) {
                "dir" -> {
                    binding.fileIcon.setImageResource(R.drawable.ic_folder)
                    binding.fileIcon.setTint(getColorFromResources(itemView.context, R.color.primary_500))
                    binding.fileSize.visibility = android.view.View.GONE
                }
                "file" -> {
                    binding.fileIcon.setImageResource(R.drawable.ic_file)
                    binding.fileIcon.setTint(getColorFromResources(itemView.context, R.color.text_secondary))
                    binding.fileSize.text = formatFileSize(fileItem.size)
                    binding.fileSize.visibility = android.view.View.VISIBLE
                }
            }
            
            // Git status (placeholder for now)
            binding.gitStatusIcon.visibility = android.view.View.GONE
            
            // Click listener
            binding.root.setOnClickListener { onItemClick(fileItem) }
        }
        
        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${String.format("%.1f", size / 1024.0)} KB"
                size < 1024 * 1024 * 1024 -> "${String.format("%.1f", size / (1024.0 * 1024.0))} MB"
                else -> "${String.format("%.1f", size / (1024.0 * 1024.0 * 1024.0))} GB"
            }
        }
        
        private fun getColorFromResources(context: Context, colorRes: Int): Int {
            return context.resources.getColor(colorRes, context.theme)
        }
    }
}