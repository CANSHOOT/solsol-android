package com.heyyoung.solsol.feature.settlement.domain.game

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

enum class Phase {
    IDLE,
    GATHERING,
    INSTRUCTION,
    RUNNING,
    FINISHED
}

data class Member(
    val endpointId: String,
    val displayName: String,
    val number: Int? = null,
    val isSelf: Boolean = false,
    val isHost: Boolean = false,
    val userId: String
)

data class RoomState(
    val title: String,
    val hostEndpointId: String,
    val members: List<Member>,
    val phase: Phase = Phase.IDLE,
    val winnerEndpointId: String? = null,
    val settlementAmount: Long? = null   // ✅ 추가
)

sealed class Msg(val type: String) {
    class Hello(val name: String, val userId: String) : Msg("HELLO")
    class RoomUpdate(val state: RoomState) : Msg("ROOM_UPDATE")
    class AssignNumbers(val assignments: Map<String, Int>) : Msg("ASSIGN_NUMBERS")
    class StartInstruction(val seconds: Int) : Msg("START_INSTRUCTION")
    class StartGame(
        val winnerEndpointId: String,
        val cycles: Int,
        val tickMs: Long,
        val order: List<String>
    ) : Msg("START_GAME")

    fun toJson(): String = gson.toJson(this)

    companion object {
        private val gson = Gson()

        fun parse(jsonString: String): Msg? {
            return try {
                val baseMsg = gson.fromJson(jsonString, BaseMsg::class.java)
                when (baseMsg.type) {
                    "HELLO" -> gson.fromJson(jsonString, Hello::class.java)
                    "ROOM_UPDATE" -> gson.fromJson(jsonString, RoomUpdate::class.java)
                    "ASSIGN_NUMBERS" -> gson.fromJson(jsonString, AssignNumbers::class.java)
                    "START_INSTRUCTION" -> gson.fromJson(jsonString, StartInstruction::class.java)
                    "START_GAME" -> gson.fromJson(jsonString, StartGame::class.java)
                    else -> null
                }
            } catch (e: JsonSyntaxException) {
                null
            }
        }

        private data class BaseMsg(val type: String)
    }
}

enum class Role {
    NONE,
    HOST,
    PARTICIPANT
}