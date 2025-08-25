package com.heyyoung.solsol.core.network

import android.util.Log
import retrofit2.Response
import retrofit2.http.*

/**
 * 신한은행 SSAFY OPEN API 서비스
 *
 * Base URL: https://finopenapi.ssafy.io/ssafy/api/v1/
 *
 * 이 인터페이스는 Retrofit이 자동으로 구현체를 생성합니다.
 * HTTP 요청을 Kotlin 함수로 간단하게 정의할 수 있습니다.
 */
interface ShinhanApiService {

    companion object {
        private const val TAG = "ShinhanApiService"
    }

    /**
     * 사용자 계정 생성
     * POST /member/
     *
     * 이메일 기반으로 신한은행 사용자 계정을 생성합니다.
     * 이미 존재하는 사용자면 에러 응답을 받습니다.
     */
    @POST("member/")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<CreateUserResponse>

    /**
     * 사용자 계정 조회
     * POST /member/search
     *
     * userId로 기존 사용자를 찾습니다.
     * 로그인 시 사용자 존재 여부 확인용입니다.
     */
    @POST("member/search")
    suspend fun searchUser(
        @Body request: SearchUserRequest
    ): Response<SearchUserResponse>

    /**
     * 수신상품 생성 (통장 상품 생성)
     * POST /edu/demandDeposit/createDemandDeposit
     *
     * 계좌를 만들기 전에 먼저 통장 상품을 생성해야 합니다.
     * "쏠쏠해영 통장" 같은 상품을 만듭니다.
     */
    @POST("edu/demandDeposit/createDemandDeposit")
    suspend fun createDemandDeposit(
        @Body request: CreateDemandDepositRequest
    ): Response<CreateDemandDepositResponse>

    /**
     * 계좌 생성
     * POST /edu/demandDeposit/createDemandDepositAccount
     *
     * 실제 계좌번호를 발급받습니다.
     * 수신상품이 먼저 생성되어야 합니다.
     */
    @POST("edu/demandDeposit/createDemandDepositAccount")
    suspend fun createBankAccount(
        @Body request: CreateAccountRequest
    ): Response<CreateAccountResponse>

    /**
     * 잔액 조회
     * POST /edu/demandDeposit/inquireDemandDepositAccountBalance
     *
     * 계좌의 현재 잔액을 조회합니다.
     * 홈화면에 표시할 잔액 정보를 가져옵니다.
     */
    @POST("edu/demandDeposit/inquireDemandDepositAccountBalance")
    suspend fun getAccountBalance(
        @Body request: BalanceRequest
    ): Response<BalanceResponse>

    /**
     * 계좌 이체 (송금)
     * POST /edu/demandDeposit/updateDemandDepositAccountTransfer
     *
     * 계좌간 돈을 이체합니다.
     * QR 결제나 더치페이에서 사용됩니다.
     */
    @POST("edu/demandDeposit/updateDemandDepositAccountTransfer")
    suspend fun transferMoney(
        @Body request: TransferRequest
    ): Response<TransferResponse>
}

// ========== Request 데이터 클래스들 ==========

/**
 * 사용자 생성 요청
 */
data class CreateUserRequest(
    val apiKey: String,
    val userId: String
) {
    init {
        Log.v("CreateUserRequest", "사용자 생성 요청: userId=$userId")
    }
}

/**
 * 사용자 조회 요청
 */
data class SearchUserRequest(
    val apiKey: String,
    val userId: String
) {
    init {
        Log.v("SearchUserRequest", "사용자 조회 요청: userId=$userId")
    }
}

/**
 * 수신상품 생성 요청
 */
data class CreateDemandDepositRequest(
    val Header: RequestHeader,
    val bankCode: String = "001",           // 신한은행 코드
    val accountName: String,                // 상품명 (예: "쏠쏠해영 통장")
    val accountDescription: String          // 상품 설명
) {
    init {
        Log.v("CreateDemandDepositRequest", "수신상품 생성: $accountName")
    }
}

/**
 * 계좌 생성 요청
 */
data class CreateAccountRequest(
    val Header: RequestHeader,
    val accountTypeUniqueNo: String         // 수신상품 고유번호
) {
    init {
        Log.v("CreateAccountRequest", "계좌 생성 요청: 상품번호=$accountTypeUniqueNo")
    }
}

/**
 * 잔액 조회 요청
 */
data class BalanceRequest(
    val Header: RequestHeader,
    val accountNo: String
) {
    init {
        Log.v("BalanceRequest", "잔액 조회: 계좌=${accountNo.take(6)}***")
    }
}

/**
 * 이체 요청
 */
data class TransferRequest(
    val Header: RequestHeader,
    val depositAccountNo: String,           // 입금계좌
    val transactionBalance: Long,           // 거래금액
    val withdrawalAccountNo: String,        // 출금계좌
    val depositTransactionSummary: String,  // 입금계좌 거래요약
    val withdrawalTransactionSummary: String // 출금계좌 거래요약
) {
    init {
        Log.v("TransferRequest", "이체 요청: ${transactionBalance}원")
    }
}

/**
 * 공통 헤더 정보
 * 모든 API 요청에 포함되어야 하는 메타데이터
 */
data class RequestHeader(
    val apiName: String,                    // API 이름
    val transmissionDate: String,           // 전송일자 (YYYYMMDD)
    val transmissionTime: String,           // 전송시각 (HHMMSS)
    val institutionCode: String = "00100",  // 기관코드
    val fintechAppNo: String = "001",       // 핀테크앱번호
    val apiServiceCode: String,             // API 서비스코드
    val institutionTransactionUniqueNo: String, // 기관거래고유번호
    val apiKey: String,                     // API 키
    val userKey: String = ""                // 사용자 키
)

// ========== Response 데이터 클래스들 ==========

/**
 * 사용자 생성 응답
 */
data class CreateUserResponse(
    val status: String,
    val message: String,
    val userKey: String? = null
)

/**
 * 사용자 조회 응답
 */
data class SearchUserResponse(
    val status: String,
    val message: String,
    val userKey: String? = null,
    val created: String? = null
)

/**
 * 수신상품 생성 응답
 */
data class CreateDemandDepositResponse(
    val Header: ResponseHeader,
    val REC: DemandDepositInfo?
)

/**
 * 계좌 생성 응답
 */
data class CreateAccountResponse(
    val Header: ResponseHeader,
    val REC: AccountInfo?
)

/**
 * 잔액 조회 응답
 */
data class BalanceResponse(
    val Header: ResponseHeader,
    val REC: BalanceInfo?
)

/**
 * 이체 응답
 */
data class TransferResponse(
    val Header: ResponseHeader,
    val REC: TransferInfo?
)

/**
 * 공통 응답 헤더
 */
data class ResponseHeader(
    val responseCode: String,
    val responseMessage: String,
    val apiName: String,
    val transmissionDate: String,
    val transmissionTime: String
)

/**
 * 수신상품 정보
 */
data class DemandDepositInfo(
    val bankCode: String,
    val accountName: String,
    val accountDescription: String,
    val accountTypeUniqueNo: String         // 이 값으로 계좌 생성
)

/**
 * 계좌 정보
 */
data class AccountInfo(
    val bankCode: String,
    val accountNo: String,                  // 발급받은 계좌번호
    val accountCreatedDate: String,
    val accountExpiryDate: String
)

/**
 * 잔액 정보
 */
data class BalanceInfo(
    val accountNo: String,
    val accountBalance: Long,               // 계좌 잔액
    val accountCreatedDate: String,
    val accountExpiryDate: String
)

/**
 * 이체 정보
 */
data class TransferInfo(
    val transactionUniqueNo: String,        // 거래고유번호
    val transactionDate: String             // 거래일시
)