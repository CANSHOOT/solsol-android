package com.heyyoung.solsol.feature.settlement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.remote.dto.MySettlementSummaryResponseDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class MoneyTransferViewModel @Inject constructor(
    private val api: SettlementApiService
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _sent = MutableStateFlow<List<MoneyTransferItem>>(emptyList())
    val sent: StateFlow<List<MoneyTransferItem>> = _sent
    // "보낸요청" = 내가 보낸 정산 요청 = receivables(받을 돈)

    private val _received = MutableStateFlow<List<MoneyTransferItem>>(emptyList())
    val received: StateFlow<List<MoneyTransferItem>> = _received
    // "받은요청" = 내가 받은 정산 요청 = payables(보낼 돈)

    init { refresh() }

    private val _selectedRequest = MutableStateFlow<MoneyTransferItem?>(null)
    val selectedRequest: StateFlow<MoneyTransferItem?> = _selectedRequest

    fun selectRequest(request: MoneyTransferItem) {
        _selectedRequest.value = request
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val dto: MySettlementSummaryResponseDto = api.getMySettlementSummary()

                // 보낸요청(= receivables: 내가 받아야 할 돈)
                _sent.value = dto.receivables.map {
                    MoneyTransferItem(
                        name = it.userName,
                        amount = it.settlementAmount
                            .setScale(0, RoundingMode.HALF_UP).toLong(),
                        status = mapStatus(it.status),
                        side = TransferSide.SENT
                    )
                }

                // 받은요청(= payables: 내가 보내야 할 돈)
                _received.value = dto.payables.map {
                    MoneyTransferItem(
                        name = it.organizerName,
                        amount = it.settlementAmount
                            .setScale(0, RoundingMode.HALF_UP).toLong(),
                        status = mapStatus(it.status),
                        side = TransferSide.RECEIVED
                    )
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun mapStatus(raw: String?): MoneyTransferStatus =
        when {
            raw.equals("완료", true) || raw.equals("COMPLETED", true) -> MoneyTransferStatus.COMPLETED
            else -> MoneyTransferStatus.PENDING // "진행중"/null/기타
        }
}
