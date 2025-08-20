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
    version = 1,
    exportSchema = false
)
abstract class DutchPayDatabase : RoomDatabase() {
    abstract fun dutchPayDao(): DutchPayDao
}