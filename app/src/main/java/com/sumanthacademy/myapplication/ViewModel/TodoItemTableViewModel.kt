package com.sumanthacademy.myapplication.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sumanthacademy.myapplication.Room.TodoRepository
import com.sumanthacademy.myapplication.model.entity.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoItemTableViewModel(private val todoRepository: TodoRepository) : ViewModel() {
    val myAllTodos:LiveData<List<TodoItem>> = todoRepository.allTodos.asLiveData()
    val myAllDescTodos:LiveData<List<TodoItem>> = todoRepository.allTodosDesc.asLiveData()

    fun insertTodo(todoItem: TodoItem) = viewModelScope.launch(Dispatchers.IO) {
        todoRepository.insertTodo(todoItem)
    }

    fun updateTodo(todoItem: TodoItem) = viewModelScope.launch(Dispatchers.IO) {
        todoRepository.updateTodo(todoItem)
    }

    fun deleteTodo(todoItem: TodoItem) = viewModelScope.launch(Dispatchers.IO) {
        todoRepository.deleteTodo(todoItem)
    }
}

class TodoItemTableViewModelFactory(private val todoRepository: TodoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoItemTableViewModel::class.java)) {
            return TodoItemTableViewModel(todoRepository) as T
        } else {
            throw IllegalArgumentException("unknown view model")
        }
    }
}