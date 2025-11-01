package com.github.easymobile.repository

import android.content.Context
import android.content.SharedPreferences
import com.github.easymobile.model.Repository
import com.github.easymobile.network.GitHubApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class GitHubRepository {
    
    private val context: Context? = null // We'll pass context when needed
    
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val api: GitHubApi by lazy {
        retrofit.create(GitHubApi::class.java)
    }
    
    fun getToken(): String {
        // This will be implemented when we have access to context
        return "" // TODO: Get from SharedPreferences
    }
    
    suspend fun getUserRepositories(token: String): List<Repository> {
        val response = api.getUserRepositories("token $token")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
    
    suspend fun getRepositoryContents(
        token: String,
        owner: String,
        repo: String,
        path: String = ""
    ): List<FileItem> {
        val response = if (path.isEmpty()) {
            api.getRepositoryContents("token $token", owner, repo, "")
        } else {
            api.getRepositoryContents("token $token", owner, repo, path)
        }
        
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                404 -> Exception("Repository or path not found")
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
    
    suspend fun getFileContent(
        token: String,
        owner: String,
        repo: String,
        path: String
    ): FileContent {
        val response = api.getFileContent("token $token", owner, repo, path)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("File content is null")
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                404 -> Exception("File not found")
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
    
    suspend fun createFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        message: String
    ): FileCreateResponse {
        val request = CreateFileRequest(
            message = message,
            content = android.util.Base64.encodeToString(content.toByteArray(), android.util.Base64.NO_WRAP)
        )
        
        val response = api.createFile("token $token", owner, repo, path, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response is null")
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                409 -> Exception("File already exists")
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
    
    suspend fun updateFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        message: String,
        sha: String
    ): FileCreateResponse {
        val request = CreateFileRequest(
            message = message,
            content = android.util.Base64.encodeToString(content.toByteArray(), android.util.Base64.NO_WRAP),
            sha = sha
        )
        
        val response = api.updateFile("token $token", owner, repo, path, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response is null")
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                404 -> Exception("File not found")
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
    
    suspend fun deleteFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        message: String,
        sha: String
    ): FileCreateResponse {
        val request = DeleteFileRequest(
            message = message,
            sha = sha
        )
        
        val response = api.deleteFile("token $token", owner, repo, path, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response is null")
        } else {
            throw when (response.code()) {
                401 -> UnauthorizedException()
                404 -> Exception("File not found")
                else -> Exception("HTTP ${response.code()}: ${response.message()}")
            }
        }
    }
}

// Data classes for API requests/responses
data class FileItem(
    val name: String,
    val path: String,
    val type: String,
    val size: Long,
    val sha: String
)

data class FileContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val content: String,
    val encoding: String
)

data class CreateFileRequest(
    val message: String,
    val content: String,
    val sha: String? = null
)

data class DeleteFileRequest(
    val message: String,
    val sha: String
)

data class FileCreateResponse(
    val content: FileItem,
    val commit: CommitInfo
)

data class CommitInfo(
    val sha: String,
    val url: String,
    val message: String
)