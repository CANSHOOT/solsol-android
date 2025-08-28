package com.heyyoung.solsol.core.di

import android.util.Log
import com.heyyoung.solsol.core.auth.TokenManager
import com.heyyoung.solsol.core.network.BackendApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BackendNetworkModule {

    private const val TAG = "BackendNetworkModule"

    /**
     * 백엔드 API Base URL
     * 해커톤용 로컬 서버 주소
     */
    private const val BASE_URL = "http://3.34.3.252:8081/api/v1/"

//    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"
    /**
     * HTTP 로깅 인터셉터 제공
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        Log.d(TAG, "HTTP 로깅 인터셉터 생성")

        return HttpLoggingInterceptor { message ->
            Log.d("BackendAPI", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
            Log.d(TAG, "로깅 레벨: BODY (요청/응답 내용 표시)")
        }
    }

    /**
     * JWT 토큰 인터셉터 제공
     *
     * 모든 API 요청에 자동으로 Authorization 헤더 추가
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        Log.d(TAG, "JWT 인증 인터셉터 생성")

        return Interceptor { chain ->
            val originalRequest = chain.request()

            // 인증이 필요 없는 엔드포인트들
            val noAuthPaths = listOf("/auth/login", "/auth/signup", "/auth/refresh")
            val requestPath = originalRequest.url.encodedPath

            if (noAuthPaths.any { requestPath.contains(it) }) {
                Log.v(TAG, "인증 불필요한 경로: $requestPath")
                return@Interceptor chain.proceed(originalRequest)
            }

            // JWT 토큰 추가
            val accessToken = runBlocking {
                try {
                    tokenManager.getAccessToken().first()
                } catch (e: Exception) {
                    Log.e(TAG, "토큰 조회 실패: ${e.message}")
                    null
                }
            }

            val newRequest = if (accessToken != null) {
                Log.v(TAG, "Authorization 헤더 추가: ${requestPath}")
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                Log.v(TAG, "토큰 없음, 헤더 추가 안함: $requestPath")
                originalRequest
            }

            chain.proceed(newRequest)
        }
    }

    /**
     * OkHttp 클라이언트 제공
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        Log.d(TAG, "OkHttpClient 생성 시작")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)        // JWT 토큰 추가
            .addInterceptor(loggingInterceptor)     // HTTP 로깅
            .addInterceptor { chain ->
                // 공통 헤더 추가
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "SolsolApp-Android/1.0")
                    .build()

                Log.v(TAG, "공통 헤더 추가: ${originalRequest.url}")
                chain.proceed(requestWithHeaders)
            }
            .build()

        Log.d(TAG, "OkHttpClient 생성 완료")
        Log.d(TAG, "- 연결 타임아웃: 30초")
        Log.d(TAG, "- JWT 인증 인터셉터 적용")
        Log.d(TAG, "- HTTP 로깅 활성화")

        return client
    }

    /**
     * Retrofit 인스턴스 제공
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d(TAG, "Retrofit 인스턴스 생성")
        Log.d(TAG, "Base URL: $BASE_URL")

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d(TAG, "Retrofit 생성 완료")
        Log.d(TAG, "- JSON 컨버터: Gson")
        Log.d(TAG, "- HTTP 클라이언트: OkHttp (JWT 인증 적용)")

        return retrofit
    }

    /**
     * 백엔드 API 서비스 제공
     */
    @Provides
    @Singleton
    fun provideBackendApiService(retrofit: Retrofit): BackendApiService {
        Log.d(TAG, "BackendApiService 생성")

        val apiService = retrofit.create(BackendApiService::class.java)

        Log.d(TAG, "백엔드 API 서비스 생성 완료")
        Log.d(TAG, "사용 가능한 API:")
        Log.d(TAG, "- POST /auth/login (로그인)")
        Log.d(TAG, "- POST /auth/signup (회원가입)")
        Log.d(TAG, "- POST /auth/refresh (토큰 갱신)")

        return apiService
    }
}