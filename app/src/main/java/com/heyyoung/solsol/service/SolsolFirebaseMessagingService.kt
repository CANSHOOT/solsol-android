package com.heyyoung.solsol.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.heyyoung.solsol.MainActivity
import com.heyyoung.solsol.R

class SolsolFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "SolsolFCM"
        private const val CHANNEL_ID = "solsol_notification_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새로운 FCM 토큰: $token")
        // TODO: 서버에 토큰 전송
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        Log.d(TAG, "FCM 메시지 수신: ${remoteMessage.data}")

        // 1) data payload가 있으면 우선 처리
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
            return
        }

        // 2) 없으면 notification payload로 기본 알림
        remoteMessage.notification?.let { n ->
            showNotification(
                title = n.title ?: "정산 알림",
                body = n.body ?: "새로운 정산 요청이 있습니다",
                data = emptyMap(),
                actionType = "OPEN_MAIN"
            )
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        val groupName = data["groupName"] ?: "정산"

        when (type) {
            "DUTCH_PAY_INVITE" -> {
                Log.d(TAG, "정산 초대 알림 수신: $groupName")
                Log.d(TAG, "정산 초대 알림 수신 데이터: $data")
                showNotification(
                    title = "새로운 정산 요청",
                    body = "$groupName 정산에 참여해 주세요",
                    data = data,
                    actionType = "OPEN_SETTLEMENT"   // 👈 클릭 시 정산 화면으로
                )
            }
            else -> {
                // 그 외 타입은 기본 처리
                showNotification(
                    title = data["title"] ?: "알림",
                    body  = data["body"]  ?: "새 소식이 있어요",
                    data = data
                )
            }
        }
    }


    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        actionType: String = "OPEN_MAIN"
    ) {
        val notificationId = (System.currentTimeMillis() % 1_000_000).toInt()

        // 기본(본문 클릭) 인텐트: 기존 동작 유지
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_action", actionType)   // 기본 이동 목적지
            putExtra("group_id",   data["groupId"])
            putExtra("group_name", data["groupName"])
            putExtra("payee_name", data["payeeName"])
            putExtra("pay_amount", data["amount"])
        }
        val contentPending = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 채널 생성
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "솔솔 알림", NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(ch)
        }

        // 액션 1) 나중에 보내기: 알림만 닫기 (브로드캐스트 리시버로 처리)
        val laterIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "ACTION_LATER"
            putExtra("notification_id", notificationId)
        }
        val laterPending = PendingIntent.getBroadcast(
            this, notificationId + 1, laterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 액션 2) 바로 송금하기: 앱 열고 송금 화면으로
        // 서버가 data에 넣어주는 키 예시: amount, fromUserName
        val payNowIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            Log.d("바로 송금하기 데이터","data: $data")
            putExtra("notification_action", "PAY_NOW")
            putExtra("group_id",   data["groupId"])
            putExtra("payee_name", data["groupName"])
            putExtra("pay_amount", data["amount"])
        }
        val payNowPending = PendingIntent.getActivity(
            this, notificationId + 2, payNowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_image) // 작은 아이콘
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(contentPending) // 본문 탭 시 기본 이동
            // 👇 액션 버튼 2개 추가
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "나중에 보내기",
                laterPending
            )
            .addAction(
                android.R.drawable.ic_menu_send,
                "바로 송금하기",
                payNowPending
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(notificationId, notification)
    }

}