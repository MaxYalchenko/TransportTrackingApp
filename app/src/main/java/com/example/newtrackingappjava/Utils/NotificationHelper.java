package com.example.newtrackingappjava.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.newtrackingappjava.R;

public class NotificationHelper extends ContextWrapper {
    private static final String EDMT_CHANNEL_ID = "com.example.newtrackingappjava";
    private static  final String EDMT_CHANNEL_NAME = "NewTrackingAppJava";
    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            creareChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void creareChannel() {
        NotificationChannel edmtChannel = new NotificationChannel(EDMT_CHANNEL_ID,
                EDMT_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        edmtChannel.enableLights(false);
        edmtChannel.enableVibration(true);
        edmtChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(edmtChannel);
    }

    public NotificationManager getManager() {
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getRealtimeTrackingNotification(String title, String content, Uri defaultSound) {
        return new Notification.Builder(getApplicationContext(), EDMT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);
    }
}
