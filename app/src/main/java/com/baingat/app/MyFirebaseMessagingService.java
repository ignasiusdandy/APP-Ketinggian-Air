package com.baingat.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token){
        super.onNewToken(token);
        android.util.Log.d("FCM_TOKEN", token);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title, body;

        // Cek dari data payload dulu
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }
        // Fallback ke notification payload
        else if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } else {
            return;
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String message){

        String CHANNEL_ID = "banjir_alert3";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Banjir Alert",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, navbar_utama.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baingat)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true);

        manager.notify(1, builder.build());
    }
}
