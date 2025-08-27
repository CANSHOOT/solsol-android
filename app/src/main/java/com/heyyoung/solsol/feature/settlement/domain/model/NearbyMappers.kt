package com.heyyoung.solsol.feature.settlement.domain.model

import com.heyyoung.solsol.core.network.UserDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import android.util.Log

/**
 * Nearby API용 직렬화 가능한 UserProfile
 */
@Serializable
data class SerializableUserProfile(
    val userId: String,
    val name: String,
    val department: String,
    val studentNumber: String,
    val appVersion: String = "1.0"
)

/**
 * BackendUserDto → UserProfile 변환
 */
fun UserDto.toUserProfile(): UserProfile {
    return UserProfile(
        userId = this.userId,
        name = this.name,
        department = this.departmentName ?: "알 수 없음",
        studentNumber = this.studentNumber,
        appVersion = "1.0"
    )
}

/**
 * UserProfile → Person 변환 (정산 참여자용)
 */
fun UserProfile.toPerson(): Person {
    return Person(
        id = this.userId,
        name = this.name,
        department = this.department,
        studentId = this.studentNumber,
        isMe = false
    )
}

/**
 * UserProfile → JSON 직렬화 (Nearby 광고용)
 */
fun UserProfile.toJsonString(): String {
    return try {
        val serializable = SerializableUserProfile(
            userId = userId,
            name = name,
            department = department,
            studentNumber = studentNumber,
            appVersion = appVersion
        )
        Json.encodeToString(serializable)
    } catch (e: Exception) {
        Log.e("NearbyMappers", "UserProfile 직렬화 실패: ${e.message}")
        // 실패 시 간단한 형태로 fallback
        "$name|$department|$studentNumber|$userId"
    }
}

/**
 * JSON → UserProfile 역직렬화 (Nearby 발견용)
 */
fun UserProfile.Companion.fromJsonString(jsonString: String): UserProfile {
    return try {
        val serializable = Json.decodeFromString<SerializableUserProfile>(jsonString)
        UserProfile(
            userId = serializable.userId,
            name = serializable.name,
            department = serializable.department,
            studentNumber = serializable.studentNumber,
            appVersion = serializable.appVersion
        )
    } catch (e: Exception) {
        Log.w("NearbyMappers", "JSON 역직렬화 실패, fallback 시도: ${e.message}")
        // JSON 실패 시 간단한 형태로 파싱 시도
        parseSimpleFormat(jsonString)
    }
}

/**
 * UserProfile Companion Object
 */
object UserProfileCompanion {
    fun fromJsonString(jsonString: String): UserProfile = UserProfile.Companion.fromJsonString(jsonString)
}

/**
 * 간단한 형태 파싱 (fallback용)
 */
private fun parseSimpleFormat(simpleString: String): UserProfile {
    val parts = simpleString.split("|")
    return if (parts.size >= 4) {
        UserProfile(
            userId = parts[3],
            name = parts[0],
            department = parts[1],
            studentNumber = parts[2],
            appVersion = "1.0"
        )
    } else {
        // 파싱 실패 시 기본값
        UserProfile(
            userId = "unknown@unknown.com",
            name = simpleString.take(10), // 처음 10자만
            department = "알 수 없음",
            studentNumber = "00000000",
            appVersion = "1.0"
        )
    }
}

/**
 * NearbyUser → Person 직접 변환
 */
fun NearbyUser.toPerson(): Person {
    return this.userProfile.toPerson()
}

/**
 * 호환성 확인
 */
fun UserProfile.isCompatibleWith(other: UserProfile): Boolean {
    return this.appVersion == other.appVersion
}

/**
 * 거리 계산 (임시 - 실제로는 Nearby API에서 제공하지 않음)
 */
fun NearbyUser.getDisplayDistance(): String {
    return distance ?: "근거리" // 기본값
}