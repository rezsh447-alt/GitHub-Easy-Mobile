package com.github.easymobile.model

import com.google.gson.annotations.SerializedName

data class Repository(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("html_url")
    val htmlUrl: String,
    
    @SerializedName("clone_url")
    val cloneUrl: String,
    
    @SerializedName("language")
    val language: String?,
    
    @SerializedName("stargazers_count")
    val starsCount: Int,
    
    @SerializedName("forks_count")
    val forksCount: Int,
    
    @SerializedName("updated_at")
    val updatedAt: String
)