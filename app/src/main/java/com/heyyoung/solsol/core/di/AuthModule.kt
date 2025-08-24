package com.heyyoung.solsol.core.di

import com.heyyoung.solsol.feature.auth.data.remote.AuthApiService
import com.heyyoung.solsol.feature.auth.data.repository.AuthRepositoryImpl
import com.heyyoung.solsol.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * 인증 의존성 주입 모듈
 * - AuthRepository 구현체 바인딩
 * - AuthApiService 제공
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApiService(
            retrofit: Retrofit
        ): AuthApiService {
            return retrofit.create(AuthApiService::class.java)
        }
    }
}