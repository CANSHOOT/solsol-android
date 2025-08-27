package com.heyyoung.solsol.feature.settlement.domain.model

/**
 * UI에서 사용하던 Person 모델과 도메인 User 모델 간의 변환
 */

// UI용 Person 모델 (기존 코드와의 호환성을 위해 유지)
data class Person(
    val id: String,
    val name: String,
    val department: String,
    val studentId: String,
    val isMe: Boolean = false
)

// User -> Person 변환
fun User.toPerson() = Person(
    id = userId,
    name = name,
    department = departmentName,
    studentId = studentNumber,
    isMe = false // UI에서 설정
)

// Person -> User 변환 (필요한 경우)
fun Person.toUser() = User(
    userId = id,
    studentNumber = studentId,
    name = name,
    departmentId = 0L, // UI에서는 정확한 ID를 모름
    departmentName = department,
    councilId = 0L,
    accountNo = "",
    accountBalance = 0L,
    councilOfficer = false
)