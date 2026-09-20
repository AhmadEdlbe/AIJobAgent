package com.example.aijobagent.core.worker

import android.content.Context
import android.util.Log

/**
 * FCM stub for high-match push. Backend can POST to FCM when new high-match jobs found.
 * To enable: add google-services.json and `com.google.firebase:firebase-messaging:23.4.0` dependency,
 * then extend FirebaseMessagingService and register in AndroidManifest.
 * Currently logs and shows local notification via NotificationHelper.
 *
 * Example enable:
 *   class FcmService : FirebaseMessagingService() {
 *     override fun onMessageReceived(m: RemoteMessage) { FcmServiceStub.handleMessage(this, m.data) }
 *   }
 */
object FcmServiceStub {
    fun handleMessage(context: Context, data: Map<String, String>) {
        Log.d("FCM", "Message: $data")
        val title = data["title"] ?: "AI Job Agent"
        val body = data["body"] ?: "New high-match job"
        NotificationHelper.showReminder(context, title, body)
    }

    fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // Save to EncryptedPrefs and sync to backend /profile/fcm-token if needed
    }
}
