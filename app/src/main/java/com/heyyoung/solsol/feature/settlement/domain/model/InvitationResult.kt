package com.heyyoung.solsol.feature.settlement.domain.model

/**
 * 정산 요청 알림 발송 결과 도메인 모델
 */
data class InvitationResult(
    val groupId: Long,
    val sentCount: Int,           // 성공적으로 발송된 알림 수
    val failedCount: Int,         // 발송 실패한 알림 수
    val failedUserIds: List<String>, // 발송 실패한 사용자 ID 목록
    val message: String,          // 결과 메시지
    val isSuccess: Boolean = failedCount == 0  // 모든 알림이 성공적으로 발송되었는지
)