package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entity.toDomain
import com.example.myapplication.data.local.entity.toEntity
import com.example.myapplication.domain.model.Task
import com.example.myapplication.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }
    
    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
    
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }
    
    override suspend fun deleteTask(id: String) {
        taskDao.deleteTaskById(id)
    }
    
    override suspend fun toggleTaskCompletion(id: String) {
        taskDao.toggleTaskCompletion(id)
    }
}