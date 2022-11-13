package com.example.bembersmusic;

import static com.example.bembersmusic.ApplicationClass.ACTION_NEXT;
import static com.example.bembersmusic.ApplicationClass.ACTION_PLAY_PAUSE;
import static com.example.bembersmusic.ApplicationClass.ACTION_PREV;
import static com.example.bembersmusic.ApplicationClass.CHANNEL_ID_1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;


public class MediaAudioService extends Service {
    private final IBinder binder = new MyBinder();
    MyMediaPlayer myMediaPlayer;
    private MediaSessionCompat mediaSession;
    ArrayList<MediaItemData> mediaItems = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "MediaAudioPlayer");
    }

    public class MyBinder extends Binder {
        MediaAudioService getService() {
            // Return this instance of MediaAudioService so clients can call public methods
            return MediaAudioService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MediaAudioService", "onDestroy: MEDIA AUDIO SERVICE DESTROYED");
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("MediaAudioService", "onTaskRemoved: MEDIA AUDIO SERVICE DESTROYED TASK REMOVED");
//        myMediaPlayer.stop();
//        myMediaPlayer.reset();
//        myMediaPlayer.release();
//        stopForeground(true);
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myMediaPlayer = MyMediaPlayer.getInstance();
        Log.d("MediaAudioService", "onStartCommand: myMediaPlayer: " + myMediaPlayer);
        if(intent != null){
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY_PAUSE:
                        pausePlay();
                        break;
                    case ACTION_NEXT:
                        next();
                        break;
                    case ACTION_PREV:
                        prev();
                        break;
                }
            }
        }

        Notification notification = createNotification();
        startForeground(7, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    void updateNotification(){
        Notification notification = createNotification();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(7, notification);
    }

    Notification createNotification(){
        Intent intent1 = new Intent(this, MediaPlayerActivity.class);
        intent1.putExtra("notification", true);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        Intent playPrevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        PendingIntent playPrevPendingIntent = PendingIntent.getBroadcast(this, 0, playPrevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Intent playNextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playPauseButton = myMediaPlayer.isPlaying() ? R.drawable.ic_baseline_pause_circle_outline_24 : R.drawable.ic_baseline_play_circle_outline_24;

        return new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(myMediaPlayer.getCurrentAudio().getImage())
                .setContentTitle(myMediaPlayer.getCurrentAudio().getTitle())
                .setContentText(myMediaPlayer.getCurrentAudio().getAuthor())
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", playPrevPendingIntent)
                .addAction(playPauseButton, "Play", playPausePendingIntent)
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", playNextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    void start() {
        myMediaPlayer.start();
    }
    boolean isPlaying(){
        return myMediaPlayer.isPlaying();
    }
    void next(){
        myMediaPlayer.stepToNextAudio();
        updateNotification();
    }
    void prev(){
        myMediaPlayer.stepToPreviousAudio();
        updateNotification();
    }
    void pausePlay(){
        myMediaPlayer.pausePlay();

    }
    void playCurrentMedia(){
        myMediaPlayer.playCurrentAudio();
    }
    int getCurrentPosition(){
        return myMediaPlayer.getCurrentPosition();
    }
    void setCurrentAudioFromIndex(int index){
        myMediaPlayer.setCurrentAudioFromIndex(index);
    }
    void disableAutoplay(){
        myMediaPlayer.disableAutoplay();
    }
    void enableAutoplay(){
        myMediaPlayer.enableAutoplay();
    }
    boolean isLooping(){
        return myMediaPlayer.isLooping();
    }
    boolean isShuffle(){
        return myMediaPlayer.isShuffle();
    }
    void switchShuffle(){
        myMediaPlayer.switchShuffle();
    }
    void switchLooping(){
        myMediaPlayer.switchLooping();
    }
    void seekTo(int progress){
        myMediaPlayer.seekTo(progress);
    }
    MediaItemData getCurrentAudio(){
        return myMediaPlayer.getCurrentAudio();
    }
    ArrayList<MediaItemData> getMediaItemsList(){
        return myMediaPlayer.getAudiosList();
    }
    MyMediaPlayer createMediaPlayer(ArrayList<MediaItemData> list){
        myMediaPlayer = MyMediaPlayer.createInstance(list);
        return myMediaPlayer;
    }
    private void audioFocusChangeHandler(int focusChange){
        Log.d("XXX", "audioFocusChangeHandler: " + focusChange);
    }
}