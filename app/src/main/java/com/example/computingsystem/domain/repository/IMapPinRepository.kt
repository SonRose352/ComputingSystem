package com.example.computingsystem.domain.repository

import com.example.computingsystem.domain.model.MapPin
import kotlinx.coroutines.flow.Flow

interface IMapPinRepository {
    fun getPins(): Flow<List<MapPin>>
    suspend fun addPin(pin: MapPin)
    suspend fun updatePin(pin: MapPin)
    suspend fun deletePin(pinId: String)
    suspend fun deleteAll()
}