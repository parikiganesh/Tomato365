package com.parikiganesh.tomato365.di

import com.google.firebase.auth.FirebaseAuth
import com.parikiganesh.tomato365.data.remote.FirestoreDataSource
import com.parikiganesh.tomato365.repository.AdminRepository
import com.parikiganesh.tomato365.repository.FarmerRepository
import com.parikiganesh.tomato365.repository.MarketRepository
import com.parikiganesh.tomato365.repository.PriceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMarketRepository(dataSource: FirestoreDataSource): MarketRepository {
        return MarketRepository(dataSource)
    }

    @Provides
    @Singleton
    fun providePriceRepository(dataSource: FirestoreDataSource): PriceRepository {
        return PriceRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(
        dataSource: FirestoreDataSource,
        auth: FirebaseAuth,
        farmerRepository: FarmerRepository
    ): AdminRepository {
        return AdminRepository(dataSource, auth, farmerRepository)
    }
}
