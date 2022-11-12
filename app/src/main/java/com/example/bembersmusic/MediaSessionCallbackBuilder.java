package com.example.bembersmusic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.service.media.MediaBrowserService;
import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallbackBuilder extends MediaSessionCompat.Callback {
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    // Defined elsewhere...
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
//    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
//    private MediaStyleNotification myPlayerNotification;
    private MediaSessionCompat mediaSession;
    private MediaAudioService service;
    private MyMediaPlayer myMediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private Context context;

    public MediaSessionCallbackBuilder(Context context, AudioManager.OnAudioFocusChangeListener afChangeListener, MediaSessionCompat mediaSession, MediaAudioService service) {
        this.context = context;
        this.intentFilter = intentFilter;
        this.afChangeListener = afChangeListener;
//        this.myNoisyAudioStreamReceiver = myNoisyAudioStreamReceiver;
//        this.myPlayerNotification = myPlayerNotification;
        this.mediaSession = mediaSession;
        this.service = service;
        this.myMediaPlayer = MyMediaPlayer.getInstance();
    }

    public MediaSessionCompat.Callback build(){
        MediaSessionCompat.Callback callback = new
                MediaSessionCompat.Callback() {
                    @Override
                    public void onPlay() {
                        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        // Request audio focus for playback, this registers the afChangeListener
                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                .setOnAudioFocusChangeListener(afChangeListener)
                                .setAudioAttributes(attrs)
                                .build();

                        int result = am.requestAudioFocus(audioFocusRequest);

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            // Start the service
                            context.startService(new Intent(context, MediaBrowserService.class));
                            // Set the session active  (and update metadata and state)
                            mediaSession.setActive(true);
                            // start the player (custom call)

                            myMediaPlayer.playCurrentAudio();
                            // Register BECOME_NOISY BroadcastReceiver
//                            registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                            // Put the service in the foreground, post notification
                            NotificationBuilder notificationBuilder = new NotificationBuilder(context, mediaSession);
                            service.startForeground(1, notificationBuilder.createMusicPlayerNotification());
                        }
                    }

                    @Override
                    public void onStop() {
                        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        // Abandon audio focus
                        am.abandonAudioFocusRequest(audioFocusRequest);
//                        unregisterReceiver(myNoisyAudioStreamReceiver);
                        // Stop the service
                        service.stopSelf();
                        // Set the session inactive  (and update metadata and state)
                        mediaSession.setActive(false);
                        // stop the player (custom call)
                        myMediaPlayer.stop();
                        // Take the service out of the foreground
                        service.stopForeground(false);
                    }

                    @Override
                    public void onPause() {
                        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        // Update metadata and state
                        // pause the player (custom call)
                        myMediaPlayer.pause();
                        // unregister BECOME_NOISY BroadcastReceiver
//                        unregisterReceiver(myNoisyAudioStreamReceiver);
                        // Take the service out of the foreground, retain the notification
                        service.stopForeground(false);
                    }
                };
        return callback;
    }

}
