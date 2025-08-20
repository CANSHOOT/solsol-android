package com.heyyoung.solsol.core.di

import com.heyyoung.solsol.feature.dutchpay.data.repository.DutchPayRepositoryImpl
import com.heyyoung.solsol.feature.dutchpay.data.repository.UserRepositoryImpl
import com.heyyoung.solsol.feature.dutchpay.domain.repository.DutchPayRepository
import com.heyyoung.solsol.feature.dutchpay.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DutchPayModule {

    @Binds
    @Singleton
    abstract fun bindDutchPayRepository(
        dutchPayRepositoryImpl: DutchPayRepositoryImpl
    ): DutchPayRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}