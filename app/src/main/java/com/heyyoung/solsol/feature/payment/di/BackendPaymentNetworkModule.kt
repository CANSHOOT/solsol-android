package com.heyyoung.solsol.feature.payment.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackendPaymentNetworkModule {
    private const val TAG = "BackendPaymentNetworkModule"

    /**
     * 백엔드 API Base URL
     * 해커톤용 로컬 서버 주소
     */
    private const val BASE_URL = "http://3.34.3.252:8081/api/v1/"

    /**
     * 백엔드 결제 API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideBackendPaymentService(retrofit: Retrofit): com.heyyoung.solsol.feature.payment.domain.BackendPaymentService {
        Log.d(BackendPaymentNetworkModule.TAG, "BackendPaymentService 생성")

        val paymentService =
            retrofit.create(com.heyyoung.solsol.feature.payment.domain.BackendPaymentService::class.java)

        Log.d(TAG, "백엔드 결제 API 서비스 생성 완료")
        Log.d(TAG, "사용 가능한 결제 API:")
        Log.d(
            TAG,
            "- GET /payments/preview/{qrData} (QR 스캔 후 결제 정보 조회)"
        )

        return paymentService
    }
}