package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.MapPin
import kotlinx.coroutines.flow.Flow

interface IMapPinRepository {
    fun getPins(boardId: String): Flow<List<MapPin>>
    suspend fun addPin(pin: MapPin, boardId: String)
    suspend fun updatePin(pin: MapPin, boardId: String)
    suspend fun deletePin(pinId: String)
    suspend fun deleteAll(boardId: String)
}