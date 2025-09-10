package com.example.myapplication.presentation.tasks.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Task

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit = {},        // 🖱️ 點擊任務導航的事件
    onToggleComplete: (String) -> Unit,  // 🎯 切換完成狀態的事件
    onDelete: (String) -> Unit,          // 🗑️ 刪除任務的事件
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTaskClick() },  // 🖱️ 點擊文字區域導航到詳細頁面
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔘 完成狀態圖標 - 單獨處理點擊
                IconButton(
                    onClick = { onToggleComplete(task.id) }  // 🎯 點擊圖標切換狀態
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
                        contentDescription = if (task.isCompleted) "已完成" else "未完成",
                        tint = if (task.isCompleted) {
                            Color(0xFF4CAF50)  // 綠色
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough  // 已完成的任務加刪除線
                        } else {
                            TextDecoration.None
                        },
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (task.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // 🗑️ 刪除按鈕
            IconButton(
                onClick = { onDelete(task.id) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "刪除任務",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}