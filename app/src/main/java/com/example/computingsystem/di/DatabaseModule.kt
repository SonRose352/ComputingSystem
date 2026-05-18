package com.example.computingsystem.di

import android.content.Context
import androidx.room.Room
import com.example.computingsystem.data.local.dao.ExpressionDao
import com.example.computingsystem.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "computing_system.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExpressionDao(db: AppDatabase): ExpressionDao = db.expressionDao()
}