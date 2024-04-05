package com.example.snapparkuser;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // You can customize how you handle the notification here.
            // For example, you can show it in a notification tray.
            sendNotification(title, body);
        }
    }

    private void sendNotification(String title, String body) {
        // Build your notification here.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "channel_id")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.snappark)
                        .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Show the notification.
        notificationManager.notify(0, notificationBuilder.build());
    }
}
