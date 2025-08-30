# 🏦 쏠쏠해영 (Solsol) - Android 앱

> **"쏠쏠하게 솔루션을 제공하는"** 모바일 결제 및 정산 애플리케이션

<div align="center">
  <img width="300" height="606" alt="Image" src="https://github.com/user-attachments/assets/86a25926-8d1f-42aa-a11c-928f53efdf4f" />
</div>

## 📱 프로젝트 개요

**쏠쏠해영(Solsol)**은 QR 코드 기반 결제, 정산, 송금, 학생회 관리 등을 통합한 Android 모바일 애플리케이션입니다. 지문 인증을 통한 보안 강화와 OCR 기술을 활용한 영수증 인식 등 최신 기술을 적용했습니다.

## ✨ 주요 기능

### 💳 **결제 시스템**
- QR 코드 스캔을 통한 카페 메뉴 선택
- 지문 인증을 통한 보안 강화
- 쿠폰 할인 및 제휴 할인 적용
- 결제 내역 조회

### 💸 **송금 시스템**
- 그룹별 송금 대상 선택
- 지문 인증을 통한 송금 실행
- 송금 완료 알림

### 🎯 **정산 시스템**
- **똑같이 나누기**: 총액을 인원수로 균등 분할
- **직접 입력**: 개별 금액 직접 입력
- **랜덤 게임**: 게임을 통한 재미있는 정산

### 🎮 **게임 정산**
- 호스트/참가자 모드
- 실시간 게임 룸
- 게임 결과에 따른 정산 금액 계산

### 🏛️ **학생회 관리**
- 지출 내역 조회
- OCR을 통한 영수증 인식 및 등록
- 회비 납부 현황 관리

### 🔔 **푸시 알림**
- 즉시 송금 알림
- 정산 요청 알림
- 실시간 알림 처리

## 🛠️ 기술 스택

### **언어 & 프레임워크**
- **Kotlin** 2.0.21
- **Jetpack Compose** (UI 프레임워크)
- **Android SDK** 35 (API 35)

### **아키텍처 & 패턴**
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture**
- **Repository Pattern**
- **Dependency Injection** (Hilt)

### **주요 라이브러리**
- **네트워킹**: Retrofit2, OkHttp3
- **데이터베이스**: Room
- **이미지 로딩**: Coil
- **QR 스캔**: ZXing
- **카메라**: CameraX
- **OCR**: ML Kit (한국어 지원)
- **지문 인증**: Biometric API
- **FCM**: Firebase Cloud Messaging
- **권한 관리**: Accompanist Permissions

## 📋 개발 환경 요구사항

### **개발 도구**
- **Android Studio**: Hedgehog | 2023.1.1 이상
- **JDK**: 11 (OpenJDK 11 또는 Oracle JDK 11)
- **Gradle**: 8.7.2
- **Kotlin**: 2.0.21

### **Android 기기 요구사항**
- **최소 SDK**: API 24 (Android 7.0 Nougat)
- **타겟 SDK**: API 35 (Android 15)
- **권장**: API 30+ (Android 11+)

## 🚀 설치 및 실행 방법

### **1. 프로젝트 클론**
```bash
git clone https://github.com/your-username/solsol-android.git
cd solsol-android
```

### **2. 개발 환경 설정**

#### **Android Studio 설정**
1. Android Studio 실행
2. `File` → `Open` → 프로젝트 폴더 선택
3. Gradle 동기화 대기

#### **JDK 설정**
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-11

# macOS/Linux
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
```

### **3. Firebase 설정**

#### **google-services.json 파일 추가**
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. 프로젝트 생성 또는 기존 프로젝트 선택
3. Android 앱 등록
4. `google-services.json` 파일 다운로드
5. `app/` 폴더에 파일 복사

#### **Firebase 설정 확인**
```xml
<!-- app/build.gradle.kts -->
plugins {
    id("com.google.gms.google-services")
}
```

### **4. 권한 설정 확인**

#### **필수 권한**
- 카메라 접근
- 지문 인증
- 네트워크 접근
- 알림 권한

#### **AndroidManifest.xml 확인**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### **5. 빌드 및 실행**

#### **Debug 빌드**
```bash
# 터미널에서
./gradlew assembleDebug

# 또는 Android Studio에서
Build → Make Project
```

#### **앱 실행**
1. Android 기기 또는 에뮬레이터 연결
2. `Run` 버튼 클릭 또는 `Shift + F10`
3. 대상 기기 선택 후 실행

## 🔧 빌드 문제 해결

### **일반적인 빌드 오류**

#### **1. Gradle 동기화 실패**
```bash
# Gradle 캐시 정리
./gradlew clean
./gradlew --stop

# Android Studio 재시작
```

#### **2. 메모리 부족 오류**
```bash
# gradle.properties 수정
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

#### **3. Kotlin 컴파일 오류**
```bash
# Kotlin 버전 확인
./gradlew --version

# 프로젝트 동기화
File → Sync Project with Gradle Files
```

### **의존성 문제 해결**

#### **라이브러리 버전 충돌**
```bash
# 의존성 트리 확인
./gradlew app:dependencies

# 충돌 해결
./gradlew app:dependencyInsight --dependency androidx.compose.ui
```

## 📱 앱 실행 및 테스트

### **실행 전 확인사항**
1. **Firebase 설정**: `google-services.json` 파일 존재 확인
2. **권한 설정**: 앱 설치 후 필요한 권한 허용
3. **지문 등록**: 기기에 지문이 등록되어 있어야 함

### **테스트 시나리오**

#### **결제 플로우 테스트**
1. 홈 화면 → QR 스캔
2. 카페 메뉴 선택
3. 결제 화면에서 지문 인증
4. 결제 완료 확인

#### **정산 플로우 테스트**
1. 홈 화면 → 정산하기
2. 정산 방식 선택 (똑같이 나누기)
3. 참여자 선택
4. 정산 생성 및 완료

#### **송금 플로우 테스트**
1. 홈 화면 → 송금하기
2. 송금 대상 선택
3. 지문 인증 후 송금 실행
4. 송금 완료 확인

## 🏗️ 프로젝트 구조

```
app/src/main/java/com/heyyoung/solsol/
├── core/                    # 공통 유틸리티
├── feature/                 # 주요 기능별 모듈
│   ├── auth/               # 인증
│   ├── home/               # 홈 화면
│   ├── payment/            # 결제
│   ├── remittance/         # 송금
│   ├── settlement/         # 정산
│   ├── studentcouncil/     # 학생회
│   └── coupon/             # 쿠폰
├── service/                 # 백그라운드 서비스
├── ui/                     # 공통 UI 컴포넌트
└── MainActivity.kt         # 메인 액티비티
```

## 🔐 보안 기능

### **지문 인증**
- 결제 시 지문 인증 필수
- 송금 시 지문 인증 필수
- Biometric API 사용

## 📊 성능 최적화

### **메모리 관리**
- 이미지 캐싱 (Coil)
- ViewModel 생명주기 관리
- 불필요한 객체 생성 방지

### **빌드 최적화**
- Gradle 빌드 캐시 활성화
- 병렬 빌드 활성화
- R8 코드 축소

```

## 📦 배포

### **Release 빌드**
```bash
./gradlew assembleRelease
```

### **APK 서명**
1. 키스토어 생성
2. `app/build.gradle.kts`에 서명 설정 추가
3. Release 빌드 실행

## 🤝 기여 방법

### **개발 환경 설정**
1. 프로젝트 포크
2. 기능 브랜치 생성
3. 코드 작성 및 테스트
4. Pull Request 생성

### **코딩 컨벤션**
- **Kotlin**: 공식 Kotlin 코딩 컨벤션 준수
- **Compose**: Material Design 3 가이드라인 준수
- **네이밍**: 명확하고 이해하기 쉬운 이름 사용


---

**쏠쏠해영**과 함께 편리하고 안전한 모바일 결제를 경험해보세요! 🚀
