package com.example.trafficlightcontrol.di

import com.example.trafficlightcontrol.data.repoImpl.LogRepositoryImpl
import com.example.trafficlightcontrol.data.repoImpl.TrafficLightRepositoryImpl
import com.example.trafficlightcontrol.data.repoImpl.UserRepositoryImpl
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
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
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindTrafficLightRepository(impl: TrafficLightRepositoryImpl): TrafficLightRepository

    @Binds
    @Singleton
    abstract fun bindLogRepository(impl: LogRepositoryImpl): LogRepository
}