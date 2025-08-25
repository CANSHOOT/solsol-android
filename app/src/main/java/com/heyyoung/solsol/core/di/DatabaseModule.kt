package com.heyyoung.solsol.core.di

import android.content.Context
import androidx.room.Room
import com.heyyoung.solsol.feature.dutchpay.data.local.DutchPayDao
import com.heyyoung.solsol.feature.dutchpay.data.local.DutchPayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDutchPayDatabase(@ApplicationContext context: Context): DutchPayDatabase {
        return Room.databaseBuilder(
            context,
            DutchPayDatabase::class.java,
            "dutchpay_database"
        )
        .fallbackToDestructiveMigration() // 스키마 변경 시 기존 데이터 삭제 후 재생성
        .build()
    }

    @Provides
    fun provideDutchPayDao(database: DutchPayDatabase): DutchPayDao {
        return database.dutchPayDao()
    }
}