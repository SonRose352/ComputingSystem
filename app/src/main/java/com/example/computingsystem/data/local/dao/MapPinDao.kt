package com.example.computingsystem.data.local.dao

import androidx.room.*
import com.example.computingsystem.data.local.entity.MapPinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapPinDao {

    @Query("SELECT * FROM map_pins ORDER BY rowid ASC")
    fun getAll(): Flow<List<MapPinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MapPinEntity)

    @Update
    suspend fun update(entity: MapPinEntity)

    @Query("DELETE FROM map_pins WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM map_pins")
    suspend fun deleteAll()
}