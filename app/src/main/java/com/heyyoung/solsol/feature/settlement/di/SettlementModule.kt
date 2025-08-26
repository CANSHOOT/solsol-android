package com.heyyoung.solsol.feature.settlement.di

import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.repository.SettlementRepositoryImpl
import com.heyyoung.solsol.feature.settlement.data.repository.UserRepositoryImpl
import com.heyyoung.solsol.feature.settlement.domain.repository.SettlementRepository
import com.heyyoung.solsol.feature.settlement.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    }
}