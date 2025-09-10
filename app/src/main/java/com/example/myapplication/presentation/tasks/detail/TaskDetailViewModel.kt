package com.example.myapplication.presentation.tasks.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Task
import com.example.myapplication.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadTask()
    }
    
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                _uiState.value = _uiState.value.copy(
                    task = task,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun toggleTaskCompletion() {
        val currentTask = _uiState.value.task ?: return
        
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(currentTask.id)
                _uiState.value = _uiState.value.copy(
                    task = currentTask.copy(isCompleted = !currentTask.isCompleted)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update task"
                )
            }
        }
    }
    
    fun deleteTask() {
        val currentTask = _uiState.value.task ?: return
        
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(currentTask.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete task"
                )
            }
        }
    }
    
    fun updateTask(title: String, description: String) {
        val currentTask = _uiState.value.task ?: return
        
        viewModelScope.launch {
            try {
                val updatedTask = currentTask.copy(
                    title = title,
                    description = description
                )
                taskRepository.updateTask(updatedTask)
                _uiState.value = _uiState.value.copy(task = updatedTask)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update task"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)