package com.example.computingsystem.data.local.dao

import androidx.room.*
import com.example.computingsystem.data.local.entity.BoardNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardNodeDao {

    @Query("SELECT * FROM board_nodes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<BoardNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BoardNodeEntity)

    @Update
    suspend fun update(entity: BoardNodeEntity)

    @Query("DELETE FROM board_nodes WHERE id = :nodeId")
    suspend fun deleteById(nodeId: String)

    @Query("DELETE FROM board_nodes")
    suspend fun deleteAll()
}