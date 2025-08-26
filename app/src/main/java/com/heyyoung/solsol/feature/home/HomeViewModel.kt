package com.heyyoung.solsol.feature.home

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
}
