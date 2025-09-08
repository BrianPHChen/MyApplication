package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Task
import com.example.myapplication.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor() : TaskRepository {
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    
    init {
        // 載入示例任務
        _tasks.value = listOf(
            Task(
                id = "1",
                title = "學習 Jetpack Compose",
                description = "完成 Compose 基礎教程"
            ),
            Task(
                id = "2", 
                title = "實現 MVVM 架構",
                description = "使用 ViewModel 和 StateFlow"
            ),
            Task(
                id = "3",
                title = "集成 Hilt 依賴注入",
                description = "設置依賴注入框架"
            ),
            Task(
                id = "4",
                title = "實作 Repository 模式",
                description = "將數據邏輯從 ViewModel 分離"
            )
        )
    }
    
    override fun getAllTasks(): Flow<List<Task>> = _tasks.asStateFlow()
    
    override suspend fun getTaskById(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }
    
    override suspend fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
    }
    
    override suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { 
            if (it.id == task.id) task else it 
        }
    }
    
    override suspend fun deleteTask(id: String) {
        _tasks.value = _tasks.value.filter { it.id != id }
    }
    
    override suspend fun toggleTaskCompletion(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                task.copy(isCompleted = !task.isCompleted)
            } else task
        }
    }
}