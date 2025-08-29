package com.heyyoung.solsol.feature.settlement.domain.game

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

// 게임 상위 페이즈
enum class Phase {
    IDLE, GATHERING, INSTRUCTION, RUNNING, FINISHED
}

// 멤버(전역 ID = userId 사용, endpointId는 로컬 식별이라 UI/표시에만 참고)
data class Member(
    val endpointId: String?,       // 로컬 엔드포인트(옵션) - 표시/디버깅용
    val displayName: String,
    val number: Int? = null,
    val isSelf: Boolean = false,
    val isHost: Boolean = false,
    val userId: String             // ★ 전역 식별자
)

// 룸 상태(승자/호스트/순서 등은 모두 userId 기준)
data class RoomState(
    val title: String,
    val hostEndpointId: String?,   // 표시/디버깅용(옵션)
    val members: List<Member>,
    val phase: Phase = Phase.IDLE,
    val winnerUserId: String? = null,   // ★ 승자 전역 ID
    val settlementAmount: Long? = null
)

// 공통 메시지(가벼운 헤더: mid, ver)
sealed class Msg(val type: String, val mid: String = java.util.UUID.randomUUID().toString(), val ver: Int = 1) {

    // 연결 직후 핸드셰이크
    class Hello(val name: String, val userId: String) : Msg("HELLO")

    // 룸 스냅샷(상태 동기화)
    class RoomUpdate(val state: RoomState) : Msg("ROOM_UPDATE")

    // 번호 배정 (키 = userId)
    class AssignNumbers(val assignments: Map<String, Int>) : Msg("ASSIGN_NUMBERS")

    // 안내(Instruction) 시작
    class StartInstruction(val seconds: Int) : Msg("START_INSTRUCTION")

    // 게임 시작 (승자/순서 = userId)
    class StartGame(
        val winnerUserId: String,
        val cycles: Int,
        val tickMs: Long,
        val order: List<String>      // userId 리스트
    ) : Msg("START_GAME")

    // (레거시 호환) 단순 하이라이트 스텝
    class SpinStep(
        val currentStep: Int,
        val totalSteps: Int,
        val currentHighlightIndex: Int
    ) : Msg("SPIN_STEP")

    // 토큰 패싱용 스텝(게스트는 하이라이트만, 호스트가 드라이브)
    class CircularStep(
        val currentStep: Int,
        val totalSteps: Int,
        val nextMemberIndex: Int,
        val winnerIndex: Int
    ) : Msg("CIRCULAR_STEP")

    fun toJson(): String = gson.toJson(this)

    companion object {
        private val gson = Gson()

        fun parse(jsonString: String): Msg? {
            return try {
                val base = gson.fromJson(jsonString, BaseMsg::class.java)
                when (base?.type) {
                    "HELLO" -> gson.fromJson(jsonString, Hello::class.java)
                    "ROOM_UPDATE" -> gson.fromJson(jsonString, RoomUpdate::class.java)
                    "ASSIGN_NUMBERS" -> gson.fromJson(jsonString, AssignNumbers::class.java)
                    "START_INSTRUCTION" -> gson.fromJson(jsonString, StartInstruction::class.java)
                    "START_GAME" -> gson.fromJson(jsonString, StartGame::class.java)
                    "SPIN_STEP" -> gson.fromJson(jsonString, SpinStep::class.java)
                    "CIRCULAR_STEP" -> gson.fromJson(jsonString, CircularStep::class.java)
                    else -> null
                }
            } catch (_: JsonSyntaxException) {
                null
            }
        }

        private data class BaseMsg(val type: String)
    }
}

// 역할
enum class Role { NONE, HOST, PARTICIPANT }
