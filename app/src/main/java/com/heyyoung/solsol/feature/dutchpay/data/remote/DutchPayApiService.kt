package com.heyyoung.solsol.feature.dutchpay.data.remote

/**
 * 더치페이 관련 REST API 인터페이스
 * - 백엔드 서버와 통신하여 더치페이 CRUD 및 사용자 검색
 * - 금융 거래는 백엔드에서 처리, 앱은 결과만 받음
 */
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