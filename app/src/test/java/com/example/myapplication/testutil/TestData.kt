package com.example.myapplication.testutil

import com.example.myapplication.domain.model.Task

object TestData {
    
    val sampleTask1 = Task(
        id = "1",
        title = "測試任務 1",
        description = "第一個測試任務的描述",
        isCompleted = false
    )
    
    val sampleTask2 = Task(
        id = "2", 
        title = "測試任務 2",
        description = "第二個測試任務的描述",
        isCompleted = true
    )
    
    val sampleTask3 = Task(
        id = "3",
        title = "測試任務 3", 
        description = "第三個測試任務的描述",
        isCompleted = false
    )
    
    val sampleTasks = listOf(sampleTask1, sampleTask2, sampleTask3)
    
    fun createTask(
        id: String = "test-id",
        title: String = "測試標題",
        description: String = "測試描述",
        isCompleted: Boolean = false
    ) = Task(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted
    )
}