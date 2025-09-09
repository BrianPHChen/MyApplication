package com.example.myapplication.presentation.tasks.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.myapplication.domain.repository.TaskRepository
import com.example.myapplication.testutil.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: TaskRepository
    private lateinit var viewModel: TaskListViewModel
    
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        
        // Mock getAllTasks to return empty flow by default
        coEvery { mockRepository.getAllTasks() } returns flowOf(emptyList())
        
        viewModel = TaskListViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `ViewModel創建成功`() = runTest {
        // Given & When & Then
        assertTrue("ViewModel應該成功創建", viewModel != null)
    }
    
    @Test
    fun `addTask應該調用Repository的addTask方法`() = runTest {
        // Given
        val testTask = TestData.createTask(id = "vm-test", title = "ViewModel測試")
        
        // When
        viewModel.addTask(testTask)
        advanceUntilIdle()
        
        // Then - 驗證Repository方法被調用
        coVerify(exactly = 1) { mockRepository.addTask(testTask) }
    }
    
    @Test
    fun `toggleTaskCompletion應該調用Repository的toggleTaskCompletion方法`() = runTest {
        // Given
        val taskId = "test-task-id"
        
        // When
        viewModel.toggleTaskCompletion(taskId)
        advanceUntilIdle()
        
        // Then
        coVerify(exactly = 1) { mockRepository.toggleTaskCompletion(taskId) }
    }
    
    @Test
    fun `deleteTask應該調用Repository的deleteTask方法`() = runTest {
        // Given
        val taskId = "test-task-id"
        
        // When
        viewModel.deleteTask(taskId)
        advanceUntilIdle()
        
        // Then
        coVerify(exactly = 1) { mockRepository.deleteTask(taskId) }
    }
    
    @Test
    fun `tasks StateFlow應該來自Repository的getAllTasks`() = runTest {
        // Given
        val testTasks = listOf(
            TestData.createTask(id = "1", title = "任務1"),
            TestData.createTask(id = "2", title = "任務2")
        )
        coEvery { mockRepository.getAllTasks() } returns flowOf(testTasks)
        
        // When - 創建新的ViewModel來測試初始化
        val newViewModel = TaskListViewModel(mockRepository)
        
        // Then - 驗證getAllTasks被調用
        coVerify(exactly = 2) { mockRepository.getAllTasks() } // 原本的 + 新的
    }
}