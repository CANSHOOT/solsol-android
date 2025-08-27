package com.heyyoung.solsol.feature.settlement.di

import android.content.Context
import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.repository.SettlementRepositoryImpl
import com.heyyoung.solsol.feature.settlement.data.repository.UserRepositoryImpl
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import com.heyyoung.solsol.feature.settlement.domain.repository.UserRepository
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyConnectionsManager
import com.heyyoung.solsol.feature.settlement.domain.nearby.NearbyPermissionManager
import com.heyyoung.solsol.core.network.BackendAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettlementModule {

    @Binds
    @Singleton
    abstract fun bindSettlementRepository(
        settlementRepositoryImpl: SettlementRepositoryImpl
    ): SettlementRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettlementApiService(retrofit: Retrofit): SettlementApiService {
            return retrofit.create(SettlementApiService::class.java)
        }
        
        @Provides
        @Singleton
        fun provideNearbyConnectionsManager(
            @ApplicationContext context: Context,
            backendAuthRepository: BackendAuthRepository
        ): NearbyConnectionsManager {
            return NearbyConnectionsManager(context, backendAuthRepository)
        }
        
        @Provides
        @Singleton
        fun provideNearbyPermissionManager(
            @ApplicationContext context: Context
        ): NearbyPermissionManager {
            return NearbyPermissionManager(context)
        }
    }
}