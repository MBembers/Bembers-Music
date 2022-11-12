package com.example.bembersmusic;

import static com.example.bembersmusic.ApplicationClass.ACTION_NEXT;
import static com.example.bembersmusic.ApplicationClass.ACTION_PLAY_PAUSE;
import static com.example.bembersmusic.ApplicationClass.ACTION_PREV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, MediaAudioService.class);
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }
}
