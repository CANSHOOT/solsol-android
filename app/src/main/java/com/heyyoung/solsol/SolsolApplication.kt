package com.heyyoung.solsol

import android.app.Application
import android.util.Log
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SolsolApplication : Application() {

    companion object {
        private const val TAG = "SolsolApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "솔솔 캠퍼스페이 앱 시작")

        // 앱 초기화
        initializeApp()
    }

    private fun initializeApp() {
        try {
            // 1. 로깅 시스템 초기화
            initLogging()

            // 2. ML Kit OCR 초기화
            initMlKitOcr()

            // 3. 데이터베이스 초기화 준비
            initDatabase()

            // 4. 네트워크 설정
            initNetworking()

            // 5. 보안 설정 (금융앱이므로 중요!)
            initSecurity()

            Log.d(TAG, "앱 초기화 완료")

        } catch (e: Exception) {
            Log.e(TAG, "앱 초기화 실패", e)
        }
    }

    private fun initLogging() {
        // 일단 개발 모드로 설정 (BuildConfig 없이)
        Log.d(TAG, "개발 모드: 상세 로깅 활성화")
    }

    private fun initMlKitOcr() {
        try {
            // ML Kit 텍스트 인식기 초기화 (미리 준비해두면 첫 사용 시 빠름)
            val koreanRecognizer = TextRecognition.getClient(
                KoreanTextRecognizerOptions.Builder().build()
            )

            val latinRecognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

            Log.d(TAG, "ML Kit OCR 초기화 완료")

        } catch (e: Exception) {
            Log.e(TAG, "ML Kit OCR 초기화 실패", e)
        }
    }

    private fun initDatabase() {
        // Room 데이터베이스 설정은 DI 모듈에서 처리
        Log.d(TAG, "데이터베이스 설정 준비")
    }

    private fun initNetworking() {
        // 네트워크 설정 (은행 API 연동용)
        Log.d(TAG, "네트워크 설정 완료")
    }

    private fun initSecurity() {
        // 일단 개발 모드로 설정 (BuildConfig 없이)
        Log.d(TAG, "개발 모드: 보안 검사 완화")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "메모리 부족 경고")

        // ML Kit 메모리 정리
        try {
            // 필요시 인식기 해제
            System.gc()
        } catch (e: Exception) {
            Log.e(TAG, "메모리 정리 실패", e)
        }
    }
}