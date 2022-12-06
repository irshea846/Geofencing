package com.rshea.geofencing.di

import com.rshea.geofencing.data.datasources.dto.LocationDataSource
import com.rshea.geofencing.data.repository.LocationRepository
import com.rshea.geofencing.data.repository.LocationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun providesLocationRepository(locationDataSource: LocationDataSource): LocationRepository {
        return LocationRepositoryImpl(locationDataSource)
    }

}