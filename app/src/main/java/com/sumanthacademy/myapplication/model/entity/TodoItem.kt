package com.sumanthacademy.myapplication.model.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "todoItem_table")
class TodoItem(val image: Int = 0, val title:String, val status:String):Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}