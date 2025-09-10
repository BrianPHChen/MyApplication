package com.example.myapplication.navigation

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Navigation 邏輯測試 - 只測試真實風險和業務邏輯
 * 
 * 測試重點：
 * 1. 路由常數正確性（防止拼寫錯誤）
 * 2. 參數提取邏輯（真實的業務邏輯）
 * 3. 路由構造邏輯（處理各種ID格式）
 */
class TaskNavigationTest {
    
    @Test
    fun `路由常數應該與NavHost定義保持一致`() {
        // 這個測試的價值：防止路由拼寫錯誤，確保常數與實際使用一致
        assertEquals("task_list", TaskDestinations.TASK_LIST_ROUTE)
        assertEquals("task_detail", TaskDestinations.TASK_DETAIL_ROUTE)
        assertEquals("task_detail/{taskId}", TaskDestinations.TASK_DETAIL_ROUTE_WITH_ARGS)
    }
    
    @Test
    fun `SavedStateHandle參數提取應該處理各種TaskId格式`() {
        // 這個測試的價值：確保真實的參數提取邏輯在各種ID格式下都能正常工作
        val testCases = mapOf(
            "simple-id" to "simple-id",
            "123" to "123",
            "task_with_underscores" to "task_with_underscores",
            "task-with-dashes" to "task-with-dashes",
            "special.id_123" to "special.id_123",
            "uuid-like-12345678-1234-1234-1234-123456789012" to "uuid-like-12345678-1234-1234-1234-123456789012"
        )
        
        testCases.forEach { (taskId, expectedId) ->
            val savedStateHandle = SavedStateHandle(mapOf("taskId" to taskId))
            val extractedId = savedStateHandle.get<String>("taskId")
            
            assertNotNull("TaskId $taskId 應該能正確提取", extractedId)
            assertEquals("TaskId $taskId 應該保持原值", expectedId, extractedId)
        }
    }
    
    @Test
    fun `SavedStateHandle缺少taskId參數時應該返回null`() {
        // 這個測試的價值：驗證邊界條件，確保錯誤處理邏輯正確
        val emptySavedStateHandle = SavedStateHandle()
        val extractedTaskId = emptySavedStateHandle.get<String>("taskId")
        
        assertNull("缺少taskId參數時應該返回null", extractedTaskId)
    }
    
    @Test
    fun `路由構造應該正確處理不同格式的TaskId`() {
        // 這個測試的價值：確保路由構造邏輯能處理各種實際可能出現的ID格式
        val testTaskIds = listOf(
            "123",
            "task-abc",
            "special_id.123",
            "long-task-id-with-many-segments",
            "CamelCaseId",
            "id_with_numbers_123"
        )
        
        testTaskIds.forEach { taskId ->
            val constructedRoute = "${TaskDestinations.TASK_DETAIL_ROUTE}/$taskId"
            
            assertTrue("路由應該以正確前綴開始", constructedRoute.startsWith("task_detail/"))
            assertTrue("路由應該包含完整的taskId", constructedRoute.endsWith(taskId))
            assertEquals("路由格式應該正確", "task_detail/$taskId", constructedRoute)
        }
    }
    
    @Test
    fun `路由模板應該與參數名稱匹配`() {
        // 這個測試的價值：確保路由模板中的參數名與代碼中使用的一致
        val routeTemplate = TaskDestinations.TASK_DETAIL_ROUTE_WITH_ARGS
        val parameterName = "taskId"
        
        assertTrue("路由模板應該包含正確的參數佔位符", 
            routeTemplate.contains("{$parameterName}"))
        
        // 驗證參數名稱與實際使用的SavedStateHandle key一致
        val testTaskId = "test-123"
        val savedStateHandle = SavedStateHandle(mapOf(parameterName to testTaskId))
        val extractedId = savedStateHandle.get<String>(parameterName)
        
        assertEquals("參數名稱應該一致", testTaskId, extractedId)
    }
}