package com.sumanthacademy.myapplication.Room

import androidx.annotation.WorkerThread
import com.sumanthacademy.myapplication.model.entity.TodoItem
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDAO: TodoDAO) {
    val allTodos: Flow<List<TodoItem>> = todoDAO.getAllTodos()
    val allTodosDesc: Flow<List<TodoItem>> = todoDAO.getAllTodosDesc()

    @WorkerThread
    suspend fun insertTodo(todoItem:TodoItem) {
        todoDAO.insertTodo(todoItem)
    }

    @WorkerThread
    suspend fun updateTodo(todoItem:TodoItem) {
        todoDAO.updateTodo(todoItem)
    }

    @WorkerThread
    suspend fun deleteTodo(todoItem:TodoItem) {
        todoDAO.deleteTodo(todoItem)
    }
}