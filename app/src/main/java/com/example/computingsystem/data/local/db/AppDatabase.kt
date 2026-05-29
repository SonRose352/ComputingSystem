package com.example.computingsystem.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.computingsystem.data.local.dao.BoardDao
import com.example.computingsystem.data.local.dao.BoardNodeDao
import com.example.computingsystem.data.local.dao.ExpressionDao
import com.example.computingsystem.data.local.dao.MapPinDao
import com.example.computingsystem.data.local.entity.BoardEntity
import com.example.computingsystem.data.local.entity.BoardNodeEntity
import com.example.computingsystem.data.local.entity.ExpressionEntity
import com.example.computingsystem.data.local.entity.MapPinEntity


@Database(
    entities = [
        BoardEntity::class,
        ExpressionEntity::class,
        BoardNodeEntity::class,
        MapPinEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
    abstract fun expressionDao(): ExpressionDao
    abstract fun boardNodeDao(): BoardNodeDao
    abstract fun mapPinDao(): MapPinDao
}