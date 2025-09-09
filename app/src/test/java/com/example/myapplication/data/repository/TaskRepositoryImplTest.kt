package com.example.myapplication.data.repository

import app.cash.turbine.test
import com.example.myapplication.domain.model.Task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        repository = TaskRepositoryImpl()
    }

    @Test
    fun `getAllTasks returns initial tasks`() = runTest {
        // When
        repository.getAllTasks().test {
            val tasks = awaitItem()
            
            // Then
            assertEquals(4, tasks.size)
            assertEquals("學習 Jetpack Compose", tasks[0].title)
            assertEquals("實現 MVVM 架構", tasks[1].title)
            assertEquals("集成 Hilt 依賴注入", tasks[2].title)
            assertEquals("實作 Repository 模式", tasks[3].title)
            
            // 確認所有任務初始狀態都是未完成
            assertFalse(tasks.all { it.isCompleted })
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTask should add new task to list`() = runTest {
        // Given
        val newTask = Task(
            id = "test-1",
            title = "測試任務",
            description = "測試描述"
        )

        // When
        repository.addTask(newTask)

        // Then
        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(5, tasks.size)
            
            val addedTask = tasks.find { it.id == "test-1" }
            assertEquals(newTask, addedTask)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTaskById should return correct task`() = runTest {
        // When
        val task = repository.getTaskById("1")

        // Then
        assertEquals("1", task?.id)
        assertEquals("學習 Jetpack Compose", task?.title)
    }

    @Test
    fun `getTaskById should return null for non-existent task`() = runTest {
        // When
        val task = repository.getTaskById("non-existent")

        // Then
        assertNull(task)
    }

    @Test
    fun `updateTask should modify existing task`() = runTest {
        // Given
        val updatedTask = Task(
            id = "1",
            title = "更新的標題",
            description = "更新的描述",
            isCompleted = true
        )

        // When
        repository.updateTask(updatedTask)

        // Then
        repository.getAllTasks().test {
            val tasks = awaitItem()
            val task = tasks.find { it.id == "1" }
            
            assertEquals("更新的標題", task?.title)
            assertEquals("更新的描述", task?.description)
            assertTrue(task?.isCompleted == true)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteTask should remove task from list`() = runTest {
        // When
        repository.deleteTask("1")

        // Then
        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(3, tasks.size)
            assertNull(tasks.find { it.id == "1" })
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTaskCompletion should toggle completion status`() = runTest {
        // Given - 初始狀態為未完成
        repository.getAllTasks().test {
            val initialTasks = awaitItem()
            assertFalse(initialTasks.find { it.id == "1" }?.isCompleted == true)
            cancelAndIgnoreRemainingEvents()
        }

        // When - 第一次切換
        repository.toggleTaskCompletion("1")

        // Then - 應該變為完成
        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertTrue(tasks.find { it.id == "1" }?.isCompleted == true)
            cancelAndIgnoreRemainingEvents()
        }

        // When - 第二次切換
        repository.toggleTaskCompletion("1")

        // Then - 應該變回未完成
        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertFalse(tasks.find { it.id == "1" }?.isCompleted == true)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTaskCompletion should not affect non-existent task`() = runTest {
        // Given
        repository.getAllTasks().test {
            val initialTasks = awaitItem()
            val initialSize = initialTasks.size
            cancelAndIgnoreRemainingEvents()

            // When
            repository.toggleTaskCompletion("non-existent")

            // Then
            repository.getAllTasks().test {
                val tasks = awaitItem()
                assertEquals(initialSize, tasks.size)
                // 確認沒有任務被意外修改
                assertEquals(initialTasks, tasks)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `multiple operations should maintain data integrity`() = runTest {
        // Given
        val newTask1 = Task("test-1", "任務1", "描述1")
        val newTask2 = Task("test-2", "任務2", "描述2")

        // When - 執行多種操作
        repository.addTask(newTask1)
        repository.addTask(newTask2)
        repository.toggleTaskCompletion("test-1")
        repository.deleteTask("1") // 刪除初始任務
        repository.updateTask(newTask2.copy(title = "更新的任務2"))

        // Then
        repository.getAllTasks().test {
            val tasks = awaitItem()
            
            // 檢查總數：4個初始 + 2個新增 - 1個刪除 = 5個
            assertEquals(5, tasks.size)
            
            // 檢查被刪除的任務不存在
            assertNull(tasks.find { it.id == "1" })
            
            // 檢查第一個新任務被標記為完成
            val task1 = tasks.find { it.id == "test-1" }
            assertTrue(task1?.isCompleted == true)
            
            // 檢查第二個新任務被更新
            val task2 = tasks.find { it.id == "test-2" }
            assertEquals("更新的任務2", task2?.title)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}