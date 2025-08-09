package com.example.trafficlightcontrol.di

import android.content.Context
import com.example.trafficlightcontrol.data.remote.BluetoothService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothService(
        @ApplicationContext context: Context
    ): BluetoothService {
        return BluetoothService(context)
    }
}
