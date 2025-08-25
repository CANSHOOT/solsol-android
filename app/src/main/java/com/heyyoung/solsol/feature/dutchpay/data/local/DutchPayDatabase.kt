package com.heyyoung.solsol.feature.dutchpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.DutchPayGroupEntity
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.DutchPayParticipantEntity
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.UserEntity

@Database(
    entities = [
        DutchPayGroupEntity::class,
        UserEntity::class,
        DutchPayParticipantEntity::class
    ],
    version = 2, // User 모델 스키마 변경으로 버전 증가
    exportSchema = false
)
abstract class DutchPayDatabase : RoomDatabase() {
    abstract fun dutchPayDao(): DutchPayDao
}