package com.heyyoung.solsol.core.di

import android.util.Log
import com.heyyoung.solsol.core.network.ShinhanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 모듈
 *
 * Hilt를 사용하여 네트워크 관련 의존성들을 제공합니다.
 * - OkHttpClient: HTTP 통신 클라이언트
 * - Retrofit: REST API 클라이언트
 * - ShinhanApiService: 신한은행 API 서비스 인터페이스
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "NetworkModule"

    /**
     * 신한은행 API Base URL
     * SSAFY 교육용 핀테크 API 서버
     */
    private const val BASE_URL = "https://finopenapi.ssafy.io/ssafy/api/v1/"

    /**
     * HTTP 로깅 인터셉터 제공
     *
     * 개발 중에는 모든 HTTP 요청/응답을 로그로 확인할 수 있습니다.
     * 배포 시에는 로그 레벨을 NONE으로 변경해야 합니다.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        Log.d(TAG, "HTTP 로깅 인터셉터 생성")

        return HttpLoggingInterceptor { message ->
            // OkHttp 로그를 Android Log로 출력
            Log.d("HTTP", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
            Log.d(TAG, "로깅 레벨: BODY (모든 요청/응답 내용 표시)")
        }
    }

    /**
     * OkHttp 클라이언트 제공
     *
     * 네트워크 통신의 기본 설정을 담당합니다.
     * - 타임아웃 설정
     * - 로깅 인터셉터 추가
     * - 공통 헤더 설정
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        Log.d(TAG, "OkHttpClient 생성 시작")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)       // 연결 타임아웃: 30초
            .readTimeout(30, TimeUnit.SECONDS)          // 읽기 타임아웃: 30초
            .writeTimeout(30, TimeUnit.SECONDS)         // 쓰기 타임아웃: 30초
            .addInterceptor(loggingInterceptor)         // HTTP 로깅
            .addInterceptor { chain ->
                // 모든 요청에 공통 헤더 추가
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "SolsolApp/1.0")
                    .build()

                Log.v(TAG, "요청 헤더 추가: ${originalRequest.url}")
                chain.proceed(requestWithHeaders)
            }
            .build()

        Log.d(TAG, "OkHttpClient 생성 완료")
        Log.d(TAG, "- 연결 타임아웃: 30초")
        Log.d(TAG, "- 읽기 타임아웃: 30초")
        Log.d(TAG, "- 쓰기 타임아웃: 30초")

        return client
    }

    /**
     * Retrofit 인스턴스 제공
     *
     * HTTP 요청을 Kotlin 인터페이스로 변환해주는 라이브러리입니다.
     * JSON 데이터를 자동으로 객체로 변환합니다.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        Log.d(TAG, "Retrofit 인스턴스 생성")
        Log.d(TAG, "Base URL: $BASE_URL")

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d(TAG, "Retrofit 생성 완료")
        Log.d(TAG, "- JSON 컨버터: Gson")
        Log.d(TAG, "- HTTP 클라이언트: OkHttp")

        return retrofit
    }

    /**
     * 신한은행 API 서비스 제공
     *
     * Retrofit이 ShinhanApiService 인터페이스의 구현체를 자동으로 생성합니다.
     * 이 서비스를 통해 실제 API 호출이 이루어집니다.
     */
    @Provides
    @Singleton
    fun provideShinhanApiService(
        retrofit: Retrofit
    ): ShinhanApiService {
        Log.d(TAG, "ShinhanApiService 생성")

        val apiService = retrofit.create(ShinhanApiService::class.java)

        Log.d(TAG, "신한은행 API 서비스 생성 완료")
        Log.d(TAG, "사용 가능한 API:")
        Log.d(TAG, "- POST /member/ (사용자 생성)")
        Log.d(TAG, "- POST /member/search (사용자 조회)")
        Log.d(TAG, "- POST /edu/demandDeposit/createDemandDeposit (상품 생성)")
        Log.d(TAG, "- POST /edu/demandDeposit/createDemandDepositAccount (계좌 생성)")
        Log.d(TAG, "- POST /edu/demandDeposit/inquireDemandDepositAccountBalance (잔액 조회)")
        Log.d(TAG, "- POST /edu/demandDeposit/updateDemandDepositAccountTransfer (계좌 이체)")

        return apiService
    }
}