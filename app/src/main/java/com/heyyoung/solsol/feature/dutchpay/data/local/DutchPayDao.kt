package com.heyyoung.solsol.feature.dutchpay.data.local

import androidx.room.*
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.DutchPayGroupEntity
import com.heyyoung.solsol.feature.dutchpay.data.local.entities.UserEntity

@Dao
interface DutchPayDao {
    
    @Query("SELECT * FROM dutch_pay_groups WHERE organizerId = :userId ORDER BY createdAt DESC")
    suspend fun getDutchPayHistoryAsOrganizer(userId: Long): List<DutchPayGroupEntity>
    
    @Query("SELECT * FROM dutch_pay_groups WHERE groupId IN (SELECT groupId FROM dutch_pay_participants WHERE userId = :userId) ORDER BY createdAt DESC")
    suspend fun getDutchPayHistoryAsParticipant(userId: Long): List<DutchPayGroupEntity>
    
    @Query("SELECT * FROM dutch_pay_groups WHERE groupId = :groupId")
    suspend fun getDutchPayById(groupId: Long): DutchPayGroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDutchPay(dutchPay: DutchPayGroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<com.heyyoung.solsol.feature.dutchpay.data.local.entities.DutchPayParticipantEntity>)
    
    
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR studentNumber LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 20")
    suspend fun searchUsers(query: String): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Long): UserEntity?
    
    @Query("DELETE FROM dutch_pay_groups WHERE groupId = :groupId")
    suspend fun deleteDutchPay(groupId: Long)
    
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}