package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    
    fun getAllTasks(): Flow<List<Task>>
    
    suspend fun getTaskById(id: String): Task?
    
    suspend fun addTask(task: Task)
    
    suspend fun updateTask(task: Task)
    
    suspend fun deleteTask(id: String)
    
    suspend fun toggleTaskCompletion(id: String)
}