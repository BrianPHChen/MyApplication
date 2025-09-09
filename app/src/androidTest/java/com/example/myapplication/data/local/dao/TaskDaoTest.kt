package com.example.myapplication.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.myapplication.data.local.database.TaskDatabase
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TaskDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only
            .build()
        taskDao = database.taskDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertTask_and_getAllTasks() = runTest {
        // Given
        val task = TaskEntity(
            id = "test-1",
            title = "Test Task",
            description = "Test Description",
            isCompleted = false
        )
        
        // When
        taskDao.insertTask(task)
        
        // Then
        taskDao.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals(task, tasks[0])
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun getTaskById_returnsCorrectTask() = runTest {
        // Given
        val task1 = TaskEntity("1", "Task 1", "Description 1", false)
        val task2 = TaskEntity("2", "Task 2", "Description 2", true)
        
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        
        // When
        val result = taskDao.getTaskById("1")
        
        // Then
        assertEquals(task1, result)
    }
    
    @Test
    fun getTaskById_returnsNull_whenTaskNotFound() = runTest {
        // When
        val result = taskDao.getTaskById("non-existent")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun updateTask_updatesExistingTask() = runTest {
        // Given
        val originalTask = TaskEntity("1", "Original", "Original Desc", false)
        val updatedTask = TaskEntity("1", "Updated", "Updated Desc", true)
        
        taskDao.insertTask(originalTask)
        
        // When
        taskDao.updateTask(updatedTask)
        
        // Then
        val result = taskDao.getTaskById("1")
        assertEquals("Updated", result?.title)
        assertEquals("Updated Desc", result?.description)
        assertTrue(result?.isCompleted == true)
    }
    
    @Test
    fun deleteTaskById_removesTask() = runTest {
        // Given
        val task = TaskEntity("1", "To Delete", "Description", false)
        taskDao.insertTask(task)
        
        // When
        taskDao.deleteTaskById("1")
        
        // Then
        val result = taskDao.getTaskById("1")
        assertNull(result)
    }
    
    @Test
    fun deleteTask_removesTask() = runTest {
        // Given
        val task = TaskEntity("1", "To Delete", "Description", false)
        taskDao.insertTask(task)
        
        // When
        taskDao.deleteTask(task)
        
        // Then
        val result = taskDao.getTaskById("1")
        assertNull(result)
    }
    
    @Test
    fun toggleTaskCompletion_togglesStatus() = runTest {
        // Given
        val task = TaskEntity("1", "Toggle Test", "Description", false)
        taskDao.insertTask(task)
        
        // When - First toggle
        taskDao.toggleTaskCompletion("1")
        
        // Then - Should be completed
        var result = taskDao.getTaskById("1")
        assertTrue(result?.isCompleted == true)
        
        // When - Second toggle
        taskDao.toggleTaskCompletion("1")
        
        // Then - Should be uncompleted
        result = taskDao.getTaskById("1")
        assertFalse(result?.isCompleted == true)
    }
    
    @Test
    fun deleteAllTasks_removesAllTasks() = runTest {
        // Given
        val task1 = TaskEntity("1", "Task 1", "Description 1", false)
        val task2 = TaskEntity("2", "Task 2", "Description 2", true)
        
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        
        // When
        taskDao.deleteAllTasks()
        
        // Then
        taskDao.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(0, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun getAllTasks_emitsUpdates_whenTasksChange() = runTest {
        // Given
        val task1 = TaskEntity("1", "Task 1", "Description 1", false)
        val task2 = TaskEntity("2", "Task 2", "Description 2", true)
        
        taskDao.getAllTasks().test {
            // Initial empty state
            var tasks = awaitItem()
            assertEquals(0, tasks.size)
            
            // Insert first task
            taskDao.insertTask(task1)
            tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals(task1, tasks[0])
            
            // Insert second task
            taskDao.insertTask(task2)
            tasks = awaitItem()
            assertEquals(2, tasks.size)
            
            // Delete first task
            taskDao.deleteTaskById("1")
            tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals(task2, tasks[0])
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun insertTask_withDuplicateId_replacesExistingTask() = runTest {
        // Given
        val originalTask = TaskEntity("1", "Original", "Original Desc", false)
        val duplicateTask = TaskEntity("1", "Duplicate", "Duplicate Desc", true)
        
        // When
        taskDao.insertTask(originalTask)
        taskDao.insertTask(duplicateTask) // Should replace due to OnConflictStrategy.REPLACE
        
        // Then
        taskDao.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Duplicate", tasks[0].title)
            assertEquals("Duplicate Desc", tasks[0].description)
            assertTrue(tasks[0].isCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }
}