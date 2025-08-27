package com.heyyoung.solsol.feature.studentcouncil

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.core.network.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentCouncilViewModel @Inject constructor(
    private val api: BackendApiService
) : ViewModel() {

    // 지출 목록
    var expenditureList by mutableStateOf<List<CouncilExpenditureResponse>>(emptyList())
        private set

    // 회비 현황 (특정 feeId)
    var feeStatus by mutableStateOf<FeeStatusResponse?>(null)
        private set

    // 홈 요약
    var summary by mutableStateOf<DeptHomeSummaryResponse?>(null)
        private set

    var currentBalance by mutableStateOf<Long>(0L)
        private set

    // 로딩/에러 상태
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * 홈 요약 불러오기
     */
    fun loadDeptSummary(month: String? = null, semester: String? = "2025-1") {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                api.getDeptSummary(month, semester)
            }.onSuccess {
                summary = it
                errorMessage = null
                Log.d("DeptSummary", "result: " + summary.toString());
            }.onFailure { e ->
                errorMessage = "홈 요약 실패: ${e.message}"
                android.util.Log.e("StudentCouncilVM", "home failed", e)
            }
            isLoading = false
        }
    }

    /**
     * 지출 내역 불러오기
     */
    fun loadExpenditures(month: String? = null, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                api.getExpenditures(month = month, page = page, size = size)
            }.onSuccess {
                Log.d("StudentCouncilVM", "백엔드 응답: $it")
                expenditureList = it.items.map { item ->
                    CouncilExpenditureResponse(
                        expenditureId = item.expenditureId,
                        councilId = summary?.header?.councilId ?: 0L, // 필요시 매핑
                        amount = item.amount.toLong(),
                        description = item.description,
                        expenditureDate = item.date,
                        category = "일반",
                        approvedBy = null
                    )
                }
                currentBalance = it.header.currentBalance.toLong()
            }.onFailure { e ->
                errorMessage = "지출 내역 불러오기 실패: ${e.message}"
                Log.e("StudentCouncilVM", "지출 내역 실패", e)
            }
            isLoading = false
        }
    }

    /**
     * 회비 현황 불러오기
     */
    fun loadFeeStatus(councilId: Long, feeId: Long) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                api.getFeeStatus(councilId, feeId)
            }.onSuccess {
                feeStatus = it
                errorMessage = null
                Log.d("DeptSummary", "result: " + feeStatus.toString());
            }.onFailure { e ->
                errorMessage = "회비 현황 불러오기 실패: ${e.message}"
            }
            isLoading = false
        }
    }

    /**
     * 지출 등록
     */
    fun addExpenditure(request: CouncilExpenditureRequest) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                api.addExpenditure(request)
            }.onSuccess {
                // 새로운 지출을 목록 앞에 추가
                expenditureList = listOf(it) + expenditureList
                errorMessage = null
            }.onFailure { e ->
                errorMessage = "지출 등록 실패: ${e.message}"
            }
            isLoading = false
        }
    }

    /**
     * 회비 이체
     */
    fun transferFee(request: CouncilFeeTransferCommand, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                api.transferFee(request)
            }.onSuccess {
                onSuccess()
                errorMessage = null
            }.onFailure { e ->
                errorMessage = "회비 이체 실패: ${e.message}"
            }
            isLoading = false
        }
    }
}
