package com.heyyoung.solsol.feature.dutchpay.data.remote

import com.heyyoung.solsol.feature.dutchpay.data.remote.dto.*
import retrofit2.http.*

interface DutchPayApiService {
    
    @GET("api/v1/users/search")
    suspend fun searchUsers(
        @Query("query") query: String
    ): List<UserDto>
    
    @GET("api/v1/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Long
    ): UserDto
    
    @POST("api/v1/dutchpay")
    suspend fun createDutchPay(
        @Body request: CreateDutchPayRequest
    ): DutchPayGroupDto
    
    @GET("api/v1/dutchpay/{groupId}")
    suspend fun getDutchPayById(
        @Path("groupId") groupId: Long
    ): DutchPayGroupDto
    
    @POST("api/v1/dutchpay/{groupId}/join")
    suspend fun joinDutchPay(
        @Path("groupId") groupId: Long,
        @Body request: JoinDutchPayRequest
    ): DutchPayParticipantDto
    
    @POST("api/v1/dutchpay/{groupId}/pay")
    suspend fun sendPayment(
        @Path("groupId") groupId: Long,
        @Body request: SendPaymentRequest
    ): Boolean
    
    @GET("api/v1/dutchpay/history/{userId}")
    suspend fun getDutchPayHistory(
        @Path("userId") userId: Long
    ): List<DutchPayGroupDto>
    
    @GET("api/v1/dutchpay/participations/{userId}")
    suspend fun getUserParticipations(
        @Path("userId") userId: Long
    ): List<DutchPayParticipantDto>
}