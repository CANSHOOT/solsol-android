package com.heyyoung.solsol.core.di

import com.heyyoung.solsol.feature.dutchpay.data.remote.DutchPayApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.solsol.com/") // TODO: 실제 API URL로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDutchPayApiService(retrofit: Retrofit): DutchPayApiService {
        return retrofit.create(DutchPayApiService::class.java)
    }
}