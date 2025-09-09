package com.example.myapplication.data.repository

import app.cash.turbine.test
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.local.entity.toDomain
import com.example.myapplication.data.local.entity.toEntity
import com.example.myapplication.domain.model.Task
import com.example.myapplication.testutil.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    private val mockTaskDao = mockk<TaskDao>(relaxed = true)
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        repository = TaskRepositoryImpl(mockTaskDao)
    }

    @Test
    fun `getAllTasks should return domain tasks from DAO entities`() = runTest {
        // Given
        val entities = TestData.sampleTaskEntities
        val expectedDomainTasks = entities.map { it.toDomain() }
        coEvery { mockTaskDao.getAllTasks() } returns flowOf(entities)

        // When & Then
        repository.getAllTasks().test {
            val tasks = awaitItem()
            assertEquals(expectedDomainTasks, tasks)
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify DAO was called
        coVerify(exactly = 1) { mockTaskDao.getAllTasks() }
    }

    @Test
    fun `getTaskById should call DAO and return mapped domain task`() = runTest {
        // Given
        val entity = TestData.sampleTaskEntity1
        val expectedTask = entity.toDomain()
        coEvery { mockTaskDao.getTaskById("1") } returns entity

        // When
        val result = repository.getTaskById("1")

        // Then
        assertEquals(expectedTask, result)
        coVerify(exactly = 1) { mockTaskDao.getTaskById("1") }
    }

    @Test
    fun `getTaskById should return null when DAO returns null`() = runTest {
        // Given
        coEvery { mockTaskDao.getTaskById("non-existent") } returns null

        // When
        val result = repository.getTaskById("non-existent")

        // Then
        assertEquals(null, result)
        coVerify(exactly = 1) { mockTaskDao.getTaskById("non-existent") }
    }

    @Test
    fun `addTask should call DAO insertTask with correct entity`() = runTest {
        // Given
        val domainTask = TestData.createTask(id = "new-task")
        val expectedEntity = domainTask.toEntity()

        // When
        repository.addTask(domainTask)

        // Then
        coVerify(exactly = 1) { mockTaskDao.insertTask(expectedEntity) }
    }

    @Test
    fun `updateTask should call DAO updateTask with correct entity`() = runTest {
        // Given
        val domainTask = TestData.createTask(
            id = "update-task",
            title = "Updated Title",
            isCompleted = true
        )
        val expectedEntity = domainTask.toEntity()

        // When
        repository.updateTask(domainTask)

        // Then
        coVerify(exactly = 1) { mockTaskDao.updateTask(expectedEntity) }
    }

    @Test
    fun `deleteTask should call DAO deleteTaskById`() = runTest {
        // Given
        val taskId = "delete-task"

        // When
        repository.deleteTask(taskId)

        // Then
        coVerify(exactly = 1) { mockTaskDao.deleteTaskById(taskId) }
    }

    @Test
    fun `toggleTaskCompletion should call DAO toggleTaskCompletion`() = runTest {
        // Given
        val taskId = "toggle-task"

        // When
        repository.toggleTaskCompletion(taskId)

        // Then
        coVerify(exactly = 1) { mockTaskDao.toggleTaskCompletion(taskId) }
    }

    @Test
    fun `entity to domain mapping works correctly`() {
        // Given
        val entity = TaskEntity(
            id = "map-test",
            title = "Mapping Test",
            description = "Testing mapping",
            isCompleted = true
        )

        // When
        val domainTask = entity.toDomain()

        // Then
        assertEquals("map-test", domainTask.id)
        assertEquals("Mapping Test", domainTask.title)
        assertEquals("Testing mapping", domainTask.description)
        assertEquals(true, domainTask.isCompleted)
    }

    @Test
    fun `domain to entity mapping works correctly`() {
        // Given
        val domainTask = Task(
            id = "map-test",
            title = "Mapping Test",
            description = "Testing mapping",
            isCompleted = true
        )

        // When
        val entity = domainTask.toEntity()

        // Then
        assertEquals("map-test", entity.id)
        assertEquals("Mapping Test", entity.title)
        assertEquals("Testing mapping", entity.description)
        assertEquals(true, entity.isCompleted)
    }
    
    @Test
    fun `getAllTasks should emit updates when DAO flow emits`() = runTest {
        // Given
        val initialEntities = listOf(TestData.sampleTaskEntity1)
        val updatedEntities = listOf(TestData.sampleTaskEntity1, TestData.sampleTaskEntity2)
        
        coEvery { mockTaskDao.getAllTasks() } returns flowOf(initialEntities, updatedEntities)
        
        // When & Then
        repository.getAllTasks().test {
            // First emission
            var tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals(TestData.sampleTask1, tasks[0])
            
            // Second emission
            tasks = awaitItem()
            assertEquals(2, tasks.size)
            assertEquals(TestData.sampleTask1, tasks[0])
            assertEquals(TestData.sampleTask2, tasks[1])
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}