package com.example.computingsystem.data.repository

import com.example.computingsystem.data.local.dao.MapPinDao
import com.example.computingsystem.data.mapper.MapPinMapper
import com.example.computingsystem.domain.model.MapPin
import com.example.computingsystem.domain.repository.IMapPinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MapPinRepositoryImpl @Inject constructor(
    private val dao: MapPinDao
) : IMapPinRepository {

    override fun getPins(): Flow<List<MapPin>> =
        dao.getAll().map { list -> list.map(MapPinMapper::toDomain) }

    override suspend fun addPin(pin: MapPin) =
        dao.insert(MapPinMapper.toEntity(pin))

    override suspend fun updatePin(pin: MapPin) =
        dao.update(MapPinMapper.toEntity(pin))

    override suspend fun deletePin(pinId: String) =
        dao.deleteById(pinId)

    override suspend fun deleteAll() =
        dao.deleteAll()
}