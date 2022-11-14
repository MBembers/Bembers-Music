package com.mbembers.bembersmusic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String ACTION_PLAY_PAUSE = "PLAY_PAUSE";
    public static final String ACTION_NEXT = "PLAY_NEXT";
    public static final String ACTION_PREV = "PLAY_PREV";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_ID_1, "Channel 1", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription("Channel 1 description");
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID_2, "Channel 2", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription("Channel 2 description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
