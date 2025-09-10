package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.presentation.tasks.detail.TaskDetailScreen
import com.example.myapplication.presentation.tasks.list.TaskListScreen

object TaskDestinations {
    const val TASK_LIST_ROUTE = "task_list"
    const val TASK_DETAIL_ROUTE = "task_detail"
    const val TASK_DETAIL_ROUTE_WITH_ARGS = "task_detail/{taskId}"
}

@Composable
fun TaskNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = TaskDestinations.TASK_LIST_ROUTE
    ) {
        composable(TaskDestinations.TASK_LIST_ROUTE) {
            TaskListScreen(
                onTaskClick = { task ->
                    navController.navigate("${TaskDestinations.TASK_DETAIL_ROUTE}/${task.id}")
                }
            )
        }
        
        composable(
            route = TaskDestinations.TASK_DETAIL_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                }
            )
        ) {
            TaskDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}