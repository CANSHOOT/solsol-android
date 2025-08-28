package com.heyyoung.solsol.feature.payment.domain

/**
 * 쿠폰 타입 정의
 */
enum class CouponType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    RANDOM("랜덤 추첨", "🎰", "랜덤으로 받은 행운의 쿠폰"),
    ATTENDANCE("출석 혜택", "📅", "꾸준한 출석으로 받은 쿠폰"),
    WELCOME("신규 가입", "🎉", "신규 가입 축하 쿠폰"),
    EVENT("이벤트", "🎪", "특별 이벤트 쿠폰"),
    REWARD("리워드", "🏆", "적립 포인트로 받은 쿠폰");
    
    companion object {
        /**
         * 문자열로부터 CouponType을 찾는 함수 (백엔드 호환)
         */
        fun fromString(value: String?): CouponType {
            val result = when (value?.uppercase()) {
                "RANDOM" -> RANDOM
                "ATTENDANCE" -> ATTENDANCE
                "ATTENDANCE_RATE" -> ATTENDANCE  // 출석률 기반 쿠폰도 출석 혜택으로 매핑
                "ATTENDANCE_RAT" -> ATTENDANCE   // 백엔드에서 ATTENDANCE_RAT로 전송되는 경우도 매핑
                "WELCOME" -> WELCOME
                "EVENT" -> EVENT
                "REWARD" -> REWARD
                else -> {
                    android.util.Log.d("CouponType", "알 수 없는 쿠폰 타입: '$value', 기본값 RANDOM 사용")
                    RANDOM // 기본값
                }
            }
            android.util.Log.v("CouponType", "쿠폰 타입 매핑: '$value' -> ${result.displayName}")
            return result
        }
    }
}
