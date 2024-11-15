package com.sumanthacademy.myapplication.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sumanthacademy.myapplication.model.Todo
import com.sumanthacademy.myapplication.model.entity.TodoItem

data class TodoLive(
    var isDeleted:Boolean,
    var todo: TodoItem
)

data class TodoRemainder(
    var isSet:Boolean,
    var todo: TodoItem
)

class TodoViewModel : ViewModel() {
    var deletedData = MutableLiveData<TodoLive>()
    var remainderData = MutableLiveData<TodoRemainder>()
    var isRefreshed = MutableLiveData<Boolean>()
}