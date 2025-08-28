package com.heyyoung.solsol.feature.remittance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyyoung.solsol.feature.settlement.data.remote.SettlementApiService
import com.heyyoung.solsol.feature.settlement.data.remote.dto.PaymentResultDto
import com.heyyoung.solsol.feature.settlement.data.remote.dto.SendPaymentRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemittanceViewModel @Inject constructor(
    private val api: SettlementApiService
) : ViewModel() {
    fun clearPaymentResponse() {
        _paymentResponse.value = null
    }
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _paymentResponse = MutableStateFlow<PaymentResultDto?>(null)
    val paymentResponse: StateFlow<PaymentResultDto?> = _paymentResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun sendPayment(groupId: Long, summary: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = api.sendPayment(groupId, SendPaymentRequest(summary))
                _paymentResponse.value = response
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
