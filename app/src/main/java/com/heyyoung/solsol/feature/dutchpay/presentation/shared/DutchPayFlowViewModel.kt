package com.heyyoung.solsol.feature.dutchpay.presentation.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import com.heyyoung.solsol.feature.dutchpay.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 더치페이 플로우 전반에서 사용하는 공유 ViewModel
 * 단계별로 선택한 데이터를 관리합니다.
 */
@HiltViewModel
class DutchPayFlowViewModel @Inject constructor() : ViewModel() {
    
    companion object {
        private const val TAG = "DutchPayFlowVM"
    }
    
    private val _splitMethod = MutableStateFlow<SplitMethod?>(null)
    val splitMethod: StateFlow<SplitMethod?> = _splitMethod.asStateFlow()
    
    private val _selectedParticipants = MutableStateFlow<List<User>>(emptyList())
    val selectedParticipants: StateFlow<List<User>> = _selectedParticipants.asStateFlow()
    
    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()
    
    init {
        Log.d(TAG, "🏗️ DutchPayFlowViewModel 생성됨")
    }
    
    fun setSplitMethod(method: SplitMethod) {
        Log.d(TAG, "🔧 분할 방식 설정: $method")
        _splitMethod.value = method
    }
    
    fun setSelectedParticipants(participants: List<User>) {
        Log.d(TAG, "👥 선택된 참여자 저장: ${participants.size}명")
        participants.forEachIndexed { index, user ->
            Log.d(TAG, "   [$index] ${user.name} (${user.userId})")
        }
        _selectedParticipants.value = participants
        Log.d(TAG, "✅ 참여자 저장 완료! 현재 저장된 참여자 수: ${_selectedParticipants.value.size}")
    }
    
    fun setTotalAmount(amount: Double) {
        Log.d(TAG, "💰 총 금액 설정: $amount")
        _totalAmount.value = amount
    }
    
    fun clearAll() {
        Log.d(TAG, "🗑️ 모든 데이터 초기화")
        _splitMethod.value = null
        _selectedParticipants.value = emptyList()
        _totalAmount.value = 0.0
    }
}

enum class SplitMethod {
    EQUAL_SPLIT,    // 똑같이 나누기
    CUSTOM_SPLIT,   // 직접 입력하기
    RANDOM_GAME     // 랜덤 게임으로 정하기
}