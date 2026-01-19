package com.vrr.departureboard.di

import com.vrr.departureboard.data.api.VrrEfaApi
import com.vrr.departureboard.data.api.VrrEfaApiImpl
import com.vrr.departureboard.data.repository.DepartureRepository
import com.vrr.departureboard.data.repository.DepartureRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindVrrEfaApi(impl: VrrEfaApiImpl): VrrEfaApi

    @Binds
    @Singleton
    abstract fun bindDepartureRepository(impl: DepartureRepositoryImpl): DepartureRepository
}
