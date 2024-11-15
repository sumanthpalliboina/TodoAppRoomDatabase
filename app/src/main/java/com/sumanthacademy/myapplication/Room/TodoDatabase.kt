package com.sumanthacademy.myapplication.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sumanthacademy.myapplication.R
import com.sumanthacademy.myapplication.model.entity.TodoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [TodoItem::class], version = 1)
abstract class TodoDatabase: RoomDatabase() {

    abstract fun getTodoDao(): TodoDAO

    //singleton prevents multiple instances of db opening at same time
    companion object {
        private var INSTANCE:TodoDatabase? = null

        fun getDatabase(context: Context,scope: CoroutineScope):TodoDatabase {
            return INSTANCE ?: synchronized(this) { //syncronized prevents multiple thread access this instance at same time
                val instance = Room.databaseBuilder(context.applicationContext,TodoDatabase::class.java,"todo_database")
                    .addCallback(TodoDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TodoDatabaseCallback(private val scope:CoroutineScope): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { db ->
                scope.launch {
                    val todoDao = db.getTodoDao()
                    todoDao.insertTodo(TodoItem(R.drawable.sumanth_photo_qqqzsw,"Default Title1","Completed"))
                    todoDao.insertTodo(TodoItem(R.drawable.sumanth_photo_qqqzsw,"Default Title2","Not Started Yet"))
                }
            }
        }
    }
}