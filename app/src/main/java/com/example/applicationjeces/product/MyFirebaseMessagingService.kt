package com.example.applicationjeces.product

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "알림"
        val message = remoteMessage.notification?.body ?: "메시지 내용"

        val notification = Notification(0, title, message, System.currentTimeMillis().toString())
        // Room Database에 알림 저장
        CoroutineScope(Dispatchers.IO).launch {
            NotificationDatabase.getDatabase(applicationContext).notificationDao().addNotification(notification)
        }
    }
}
