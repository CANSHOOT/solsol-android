package com.heyyoung.solsol.feature.settlement.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.domain.model.SettlementGroup
import com.heyyoung.solsol.feature.settlement.domain.usecase.CreateSettlementGameUseCase
import com.heyyoung.solsol.feature.settlement.domain.usecase.CreateSettlementUseCase
import com.heyyoung.solsol.feature.settlement.domain.usecase.JoinSettlementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettlementEqualViewModel @Inject constructor(
    private val createSettlementUseCase: CreateSettlementUseCase,
    private val createSettlementGameUseCase: CreateSettlementGameUseCase,
    private val joinSettlementUseCase: JoinSettlementUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SettlementEqualViewModel"
    }

    private val _uiState = MutableStateFlow(SettlementEqualUiState())
    val uiState: StateFlow<SettlementEqualUiState> = _uiState.asStateFlow()

    fun createSettlement(
        organizerId: String,
        groupName: String,
        totalAmount: Double,
        participants: List<Person>
    ) {
        Log.d(TAG, "정산 요청 시작: $groupName, ${totalAmount}원, ${participants.size}명")
        
        _uiState.value = _uiState.value.copy(
            isCreating = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                // participants에서 "나"를 제외한 실제 참여자들만 추출
                val participantUserIds = participants
                    .filter { !it.isMe }
                    .map { it.id }
                
                Log.d(TAG, "참여자 ID 목록: $participantUserIds")
                
                val result = createSettlementUseCase(
                    organizerId = organizerId,
                    paymentId = System.currentTimeMillis(), // 임시 결제 ID
                    groupName = groupName,
                    totalAmount = totalAmount,
                    participantUserIds = participantUserIds
                )
                
                result.fold(
                    onSuccess = { settlementGroup ->
                        Log.d(TAG, "✅ 정산 생성 성공: groupId=${settlementGroup.groupId}")
                        Log.d(TAG, "🔄 참여자들을 그룹에 참여시키는 중...")
                        
                        // 생성 성공 후 모든 참여자를 그룹에 참여시킴
                        settlementGroup.groupId?.let {
                            val filtered = participants.filter { !it.isMe } // 자기 자신 제거
                            joinParticipantsToGroup(it, filtered, settlementGroup)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ 정산 생성 실패: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = error.message ?: "정산 요청에 실패했습니다"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ 정산 생성 예외: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = "정산 요청 중 오류가 발생했습니다"
                )
            }
        }
    }

    fun createSettlementGame(
        organizerId: String,
        groupName: String,
        totalAmount: Double,
        participants: List<Person>,
        onResult: (Long?) -> Unit
    ) {
        Log.d(TAG, "정산 요청 시작: $groupName, ${totalAmount}원, ${participants.size}명")

        _uiState.value = _uiState.value.copy(
            isCreating = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val participantUserIds = participants
                    .map { it.id }

                Log.d(TAG, "참여자 ID 목록: $participantUserIds")

                val result = createSettlementGameUseCase(
                    organizerId = organizerId,
                    paymentId = System.currentTimeMillis(), // 임시 결제 ID
                    groupName = groupName,
                    totalAmount = totalAmount,
                    participantUserIds = participantUserIds
                )

                result.fold(
                    onSuccess = { settlementGroup ->
                        Log.d(TAG, "✅ 정산 생성 성공: groupId=${settlementGroup.groupId}")
                        Log.d(TAG, "🔄 참여자들을 그룹에 참여시키는 중...")

                        // 생성 성공 후 모든 참여자를 그룹에 참여시킴
                        settlementGroup.groupId?.let {
                            joinParticipantsToGroup(it, participants, settlementGroup)
                            onResult(it)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ 정산 생성 실패: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = error.message ?: "정산 요청에 실패했습니다"
                        )
                        onResult(null)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ 정산 생성 예외: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = "정산 요청 중 오류가 발생했습니다"
                )
                onResult(null)
            }
        }
    }

    
    private suspend fun joinParticipantsToGroup(
        groupId: Long,
        participants: List<Person>,
        settlementGroup: SettlementGroup
    ) {
        var joinSuccessCount = 0
        var joinFailureCount = 0
        
        // 모든 참여자(나 포함)를 그룹에 참여시킴
        participants.forEach { participant ->
            try {
                Log.d(TAG, "👤 ${participant.name}을(를) 그룹에 참여시키는 중... (ID: ${participant.id})")
                
                // 이메일 형식 검증
                if (!participant.id.contains("@")) {
                    Log.e(TAG, "❌ ${participant.name} 잘못된 이메일 형식 - userId: ${participant.id}")
                    joinFailureCount++
                    return@forEach
                }
                
                val joinResult = joinSettlementUseCase(
                    groupId = groupId,
                    userId = participant.id, // String으로 바로 전달
                    amount = participant.amount
                )
                
                joinResult.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ ${participant.name} 그룹 참여 성공")
                        joinSuccessCount++
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ ${participant.name} 그룹 참여 실패: ${error.message}")
                        joinFailureCount++
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ ${participant.name} 그룹 참여 예외: ${e.message}")
                joinFailureCount++
            }
        }
        
        // 모든 참여 시도 완료 후 결과 처리
        Log.d(TAG, "🎯 그룹 참여 결과 - 성공: ${joinSuccessCount}명, 실패: ${joinFailureCount}명")
        
        if (joinFailureCount == 0) {
            // 모든 참여자가 성공적으로 참여한 경우
            Log.d(TAG, "🎉 모든 참여자가 그룹에 성공적으로 참여했습니다!")
            _uiState.value = _uiState.value.copy(
                isCreating = false,
                isCompleted = true,
                createdSettlement = settlementGroup
            )
        } else {
            // 일부 참여자가 참여에 실패한 경우 (하지만 정산 그룹은 생성됨)
            Log.w(TAG, "⚠️ 일부 참여자의 그룹 참여에 실패했지만 정산은 생성되었습니다")
            _uiState.value = _uiState.value.copy(
                isCreating = false,
                isCompleted = true,
                createdSettlement = settlementGroup,
                error = "${joinFailureCount}명의 참여자 추가에 실패했습니다. 나중에 다시 초대해 주세요."
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        Log.d(TAG, "🧹 정산 상태 초기화")
        _uiState.value = SettlementEqualUiState()
    }
    
    // 정산 완료 후 자동으로 상태 초기화하는 메서드
    fun onSettlementCompleteNavigated() {
        Log.d(TAG, "🧹 정산 완료 화면 진입 후 자동 상태 초기화")
        resetState()
    }
}

data class SettlementEqualUiState(
    val isCreating: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val createdSettlement: SettlementGroup? = null
)