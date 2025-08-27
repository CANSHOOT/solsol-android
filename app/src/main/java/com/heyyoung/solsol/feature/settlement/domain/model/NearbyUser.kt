package com.heyyoung.solsol.feature.settlement.domain.model

/**
 * 주변 기기 탐색으로 발견된 사용자 정보
 */
data class NearbyUser(
    val endpointId: String,        // Nearby API 기기 고유 ID
    val userProfile: UserProfile,  // 사용자 프로필 정보
    val distance: String? = null,  // 거리 정보 (선택사항)
    val isConnected: Boolean = false, // 연결 상태
    val discoveredAt: Long = System.currentTimeMillis() // 발견 시간
)

/**
 * 주변 기기로 광고할 사용자 프로필 정보
 */
data class UserProfile(
    val userId: String,      // 이메일 형태의 사용자 ID
    val name: String,        // 사용자 이름
    val department: String,  // 학과
    val studentNumber: String, // 학번
    val appVersion: String = "1.0" // 앱 버전 (호환성 확인용)
) {
    companion object
}

/**
 * Nearby 연결 상태
 */
enum class NearbyConnectionStatus {
    IDLE,           // 비활성 상태
    ADVERTISING,    // 광고 중
    DISCOVERING,    // 검색 중
    CONNECTED,      // 연결됨
    ERROR           // 오류 상태
}

/**
 * Nearby 검색 결과 상태
 */
data class NearbyDiscoveryState(
    val status: NearbyConnectionStatus = NearbyConnectionStatus.IDLE,
    val discoveredUsers: List<NearbyUser> = emptyList(),
    val error: String? = null,
    val isSearching: Boolean = false
)