package com.example.computingsystem.data.local.dao

import androidx.room.*
import com.example.computingsystem.data.local.entity.ExpressionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpressionDao {

    @Query("SELECT * FROM expressions ORDER BY createdAtTimestamp DESC")
    fun getAll(): Flow<List<ExpressionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpressionEntity)

    @Delete
    suspend fun delete(entity: ExpressionEntity)

    @Query("DELETE FROM expressions")
    suspend fun deleteAll()
}