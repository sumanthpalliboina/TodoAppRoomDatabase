package com.sumanthacademy.myapplication.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumanthacademy.myapplication.model.entity.TodoItem
import kotlinx.coroutines.flow.Flow

//note: suspend to use separate thread in background without disturbing main thread

@Dao
interface TodoDAO {

    @Insert
    suspend fun insertTodo(todoItem:TodoItem)

    @Update
    suspend fun updateTodo(todoItem:TodoItem)

    @Delete
    suspend fun deleteTodo(todoItem:TodoItem)

    @Query("SELECT * FROM todoItem_table ORDER BY id ASC")
    fun getAllTodos() : Flow<List<TodoItem>>

    @Query("SELECT * FROM todoItem_table ORDER BY id DESC")
    fun getAllTodosDesc(): Flow<List<TodoItem>>
}