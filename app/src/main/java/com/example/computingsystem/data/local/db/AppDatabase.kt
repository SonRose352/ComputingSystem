package com.example.computingsystem.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.computingsystem.data.local.dao.BoardNodeDao
import com.example.computingsystem.data.local.dao.ExpressionDao
import com.example.computingsystem.data.local.entity.BoardNodeEntity
import com.example.computingsystem.data.local.entity.ExpressionEntity


@Database(
    entities = [
        ExpressionEntity::class,
        BoardNodeEntity::class
               ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expressionDao(): ExpressionDao
    abstract fun boardNodeDao(): BoardNodeDao
}