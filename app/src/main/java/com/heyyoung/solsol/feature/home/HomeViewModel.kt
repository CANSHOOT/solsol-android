package com.heyyoung.solsol.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.network.BackendAuthRepository
import com.heyyoung.solsol.core.network.BackendApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: BackendAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _studentName = MutableStateFlow<String?>(null)
    val studentName: StateFlow<String?> = _studentName

    private val _studentNumber = MutableStateFlow<String?>(null)
    val studentNumber: StateFlow<String?> = _studentNumber

    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)


    fun loadProfile() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repo.getMyProfile()) {
                is BackendApiResult.Success -> {
                    _studentName.value = result.data.name
                    _studentNumber.value = result.data.studentNumber
                    error.value = null
                }
                is BackendApiResult.Error<*> -> {
                    error.value = result.message
                }
            }
            isLoading.value = false
        }
    }

    /**
     * 간단한 로그아웃 처리
     */
    fun logout(onLogoutComplete: () -> Unit) {
        Log.d(TAG, "로그아웃 시작")

        viewModelScope.launch {
            repo.logout() // 토큰 삭제
            
            // 로컬 상태 초기화
            _studentName.value = null
            _studentNumber.value = null
            error.value = null
            
            Log.i(TAG, "로그아웃 완료")
            onLogoutComplete() // 로그인 화면으로 이동
        }
    }
}
