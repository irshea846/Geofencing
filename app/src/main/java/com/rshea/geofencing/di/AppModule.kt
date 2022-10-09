package com.rshea.geofencing.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent:: class)
object AppModule {

    @Singleton
    @Provides
    fun provideRandomString(): String {
        return "String for DI"
    }
}