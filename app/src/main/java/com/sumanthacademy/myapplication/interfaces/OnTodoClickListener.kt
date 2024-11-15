package com.sumanthacademy.myapplication.interfaces

import com.sumanthacademy.myapplication.model.Todo
import com.sumanthacademy.myapplication.model.entity.TodoItem

interface OnTodoClickListener {
    fun onTodoItemClickListener(position:Int,tod: TodoItem)
}