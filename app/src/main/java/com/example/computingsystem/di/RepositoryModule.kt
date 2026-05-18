package com.example.computingsystem.di

import com.example.computingsystem.data.repository.BoardRepositoryImpl
import com.example.computingsystem.data.repository.ExpressionRepositoryImpl
import com.example.computingsystem.domain.repository.IBoardRepository
import com.example.computingsystem.domain.repository.IExpressionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpressionRepository(
        impl: ExpressionRepositoryImpl
    ): IExpressionRepository

    @Binds
    @Singleton
    abstract fun bindBoardRepository(
        impl: BoardRepositoryImpl
    ): IBoardRepository
}