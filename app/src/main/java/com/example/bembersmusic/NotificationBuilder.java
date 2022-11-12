package com.example.bembersmusic;

import static com.example.bembersmusic.ApplicationClass.CHANNEL_ID_1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

public class NotificationBuilder {
    private Context _context;
    NotificationChannel channel;
    NotificationManager notificationManager;
    MediaSessionCompat mediaSession;

    public NotificationBuilder(Context _context, MediaSessionCompat mediaSession) {
        this._context = _context;
    }

    public Notification createMusicPlayerNotification(){
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, CHANNEL_ID_1);

        Intent intent = new Intent(_context, MediaPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        builder
                // Add the metadata for the currently playing track
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())

                // Enable launching the player by clicking the notification
//                .setContentIntent(new PendingIntent(_context, MusicPlayerActivity.class))

                // Stop the service when the notification is swiped away
//                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
//                        PlaybackStateCompat.ACTION_STOP))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(R.drawable.music_icon)
                .setColor(ContextCompat.getColor(_context, R.color.dark_slate_blue))


                // Add a pause button
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_baseline_pause_circle_outline_24, _context.getString(R.string.pause),
                        PendingIntent.getActivity(_context, 727, intent, PendingIntent.FLAG_IMMUTABLE)).build())

//                 Take advantage of MediaStyle features
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(mediaSession.getSessionToken())
                                .setShowActionsInCompactView(0)

//                         Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(_context,
                                PlaybackStateCompat.ACTION_STOP)));

        return builder.build();
    }
}
