package com.rshea.geofencing.di

import android.content.Context
import androidx.room.Room
import com.rshea.geofencing.data.datasources.dao.GeofenceDatabase
import com.rshea.geofencing.util.Constants.GEOFENCE_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        GeofenceDatabase::class.java,
        GEOFENCE_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: GeofenceDatabase) = database.geofenceDao()
}