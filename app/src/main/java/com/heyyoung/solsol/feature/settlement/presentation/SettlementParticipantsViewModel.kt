package com.heyyoung.solsol.feature.settlement.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.settlement.domain.model.Person
import com.heyyoung.solsol.feature.settlement.domain.model.User
import com.heyyoung.solsol.feature.settlement.domain.model.toPerson
import com.heyyoung.solsol.feature.settlement.domain.usecase.SearchUsersUseCase
import com.heyyoung.solsol.core.network.BackendAuthRepository
import com.heyyoung.solsol.core.network.BackendApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettlementParticipantsViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val backendAuthRepository: BackendAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettlementParticipantsViewModel"
    }

    private val _uiState = MutableStateFlow(SettlementParticipantsUiState())
    val uiState: StateFlow<SettlementParticipantsUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Person>>(emptyList())
    val searchResults: StateFlow<List<Person>> = _searchResults.asStateFlow()

    private val _currentUser = MutableStateFlow<Person?>(null)
    val currentUser: StateFlow<Person?> = _currentUser.asStateFlow()

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        Log.d(TAG, "사용자 검색 시작: '$query'")
        
        _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
        
        viewModelScope.launch {
            searchUsersUseCase(query).fold(
                onSuccess = { users ->
                    Log.d(TAG, "✅ 검색 성공: ${users.size}명 검색됨")
                    val persons = users.map { it.toPerson() }
                    _searchResults.value = persons
                    _uiState.value = _uiState.value.copy(isSearching = false)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ 검색 실패: ${error.message}")
                    _searchResults.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false, 
                        searchError = error.message ?: "검색에 실패했습니다"
                    )
                }
            )
        }
    }

    fun clearSearchResults() {
        Log.d(TAG, "검색 결과 초기화")
        _searchResults.value = emptyList()
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    fun loadCurrentUser() {
        Log.d(TAG, "현재 사용자 정보 로드 시작")
        
        viewModelScope.launch {
            try {
                when (val result = backendAuthRepository.getMyProfile()) {
                    is BackendApiResult.Success -> {
                        val profile = result.data
                        val currentUserPerson = Person(
                            id = profile.userId, // 실제 이메일 사용
                            name = profile.name,
                            department = profile.departmentName ?: "알 수 없음",
                            studentId = profile.studentNumber,
                            isMe = true
                        )
                        _currentUser.value = currentUserPerson
                        Log.d(TAG, "✅ 현재 사용자 정보 로드 성공: ${profile.name} (${profile.userId})")
                    }
                    is BackendApiResult.Error -> {
                        Log.e(TAG, "❌ 현재 사용자 정보 로드 실패: ${result.message}")
                        // 실패 시 더미 데이터로 fallback (실제 이메일 형식으로)
                        _currentUser.value = Person("test@ssafy.com", "김신한", "컴퓨터공학과", "20251234", isMe = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 현재 사용자 정보 로드 예외: ${e.message}")
                // 예외 발생 시 더미 데이터로 fallback (실제 이메일 형식으로)
                _currentUser.value = Person("test@ssafy.com", "김신한", "컴퓨터공학과", "20251234", isMe = true)
            }
        }
    }
    
    // 정산 프로세스 완료 후 상태 초기화
    fun resetForNewSettlement() {
        Log.d(TAG, "🧹 새로운 정산을 위한 상태 초기화")
        _searchResults.value = emptyList()
        _uiState.value = SettlementParticipantsUiState()
        // currentUser는 유지 (다시 로드할 필요 없음)
    }
}

data class SettlementParticipantsUiState(
    val isSearching: Boolean = false,
    val searchError: String? = null
)