package com.heyyoung.solsol.feature.settlement.data.remote

/**
 * 정산 관련 REST API 인터페이스
 * - 백엔드 서버와 통신하여 정산 CRUD 및 사용자 검색
 * - 금융 거래는 백엔드에서 처리, 앱은 결과만 받음
 */
import com.heyyoung.solsol.feature.settlement.data.remote.dto.*
import retrofit2.http.*

interface SettlementApiService {
    
    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): List<UserDto>
    
    @GET("users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String
    ): UserDto
    
    @POST("dutchpay")
    suspend fun createSettlement(
        @Body request: CreateSettlementRequest
    ): SettlementGroupDto
    
    @POST("dutchpay/{groupId}/invite")
    suspend fun sendSettlementInvitations(
        @Path("groupId") groupId: Long,
        @Body request: SendInvitationsRequest
    ): InvitationResultDto
    
    @GET("dutchpay/{groupId}")
    suspend fun getSettlementById(
        @Path("groupId") groupId: Long
    ): SettlementGroupDto
    
    @POST("dutchpay/{groupId}/join")
    suspend fun joinSettlement(
        @Path("groupId") groupId: Long,
        @Body request: JoinSettlementRequest
    ): SettlementParticipantDto
    
    @POST("dutchpay/{groupId}/pay")
    suspend fun sendPayment(
        @Path("groupId") groupId: Long,
        @Body request: SendPaymentRequest
    ): PaymentResultDto
    
    @GET("dutchpay/history/{userId}")
    suspend fun getSettlementHistory(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): List<SettlementGroupDto>
    
    @GET("dutchpay/participations/{userId}")
    suspend fun getUserParticipations(
        @Path("userId") userId: String
    ): List<SettlementParticipantDto>
}