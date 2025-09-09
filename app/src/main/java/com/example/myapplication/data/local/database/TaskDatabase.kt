package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    companion object {
        const val DATABASE_NAME = "task_database"
        
        // 在資料庫創建時插入初始資料
        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // 插入初始任務資料
                db.execSQL("""
                    INSERT INTO tasks (id, title, description, is_completed) VALUES 
                    ('1', '學習 Jetpack Compose', '完成 Compose 基礎教程', 0),
                    ('2', '實現 MVVM 架構', '使用 ViewModel 和 StateFlow', 0),
                    ('3', '集成 Hilt 依賴注入', '設置依賴注入框架', 0),
                    ('4', '實作 Repository 模式', '將數據邏輯從 ViewModel 分離', 0)
                """)
            }
        }
    }
}