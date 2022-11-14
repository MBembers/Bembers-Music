package com.mbembers.bembersmusic;

import static com.mbembers.bembersmusic.ApplicationClass.ACTION_NEXT;
import static com.mbembers.bembersmusic.ApplicationClass.ACTION_PLAY_PAUSE;
import static com.mbembers.bembersmusic.ApplicationClass.ACTION_PREV;
import static com.mbembers.bembersmusic.ApplicationClass.CHANNEL_ID_1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;

import java.util.ArrayList;


public class MediaAudioService extends Service {
    private final IBinder binder = new MyBinder();
    private ExoPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    public MediaControllerCompat.TransportControls transportControls;
    public static final int FOREGROUND_SERVICE_ID = 7;
    ArrayList<MediaItemData> mediaItems = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        mediaPlayer = new ExoPlayer.Builder(this).build();
        mediaPlayer.setAudioAttributes(audioAttributes, true);

        mediaPlayer.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                Log.d("MediaAudioService", "onMediaItemTransition: NOTIFY AUDIO CHANGED updateSong");
                notifyAudioChanged();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                notifyAudioChanged();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    Log.d("MediaAudioService", "onMediaItemTransition: NOTIFY AUDIO STARTED updateSong");
                    notifyAudioStarted();
                }
            }
        });

        mediaSession = new MediaSessionCompat(this, "BemberMediaSession");
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(mediaPlayer);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            @NonNull
            @Override
            public MediaDescriptionCompat getMediaDescription(@NonNull Player player, int windowIndex) {
                MediaItem mediaItem = player.getMediaItemAt(windowIndex);
                byte [] imageData = mediaItem.mediaMetadata.artworkData;
                MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                        .setTitle(mediaItem.mediaMetadata.title)
                        .setDescription(mediaItem.mediaMetadata.artist);
                if(imageData != null)
                        builder.setIconBitmap(BitmapFactory.decodeByteArray(imageData, 0, mediaItem.mediaMetadata.artworkData.length));
                return builder.build();
            }
        });
        mediaController = mediaSession.getController();
        transportControls = mediaController.getTransportControls();

        // probably set callback but idk
        mediaSession.setActive(true);
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
        Log.d("MediaAudioService", "onStartCommand: exoPlayer: " + mediaPlayer);
        if(intent != null){
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY_PAUSE:
                        if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
                            transportControls.pause();
                        else
                            transportControls.play();
                        break;
                    case ACTION_NEXT:
                        Log.d("MediaAudioService", "onStart ACTION_NEXT: ");
                        transportControls.skipToNext();
                        transportControls.play();
                        break;
                    case ACTION_PREV:
                        transportControls.skipToPrevious();
                        transportControls.play();
                        break;
                }
            }
        }

        Notification notification = createNotification();
        startForeground(FOREGROUND_SERVICE_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    void updateNotification(){
        Notification notification = createNotification();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(FOREGROUND_SERVICE_ID, notification);
    }

    Notification createNotification(){
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.putExtra("notification", true);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);

        Intent playPrevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        PendingIntent playPrevPendingIntent = PendingIntent.getBroadcast(this, 0, playPrevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playNextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent playNextPendingIntent = PendingIntent.getBroadcast(this, 0, playNextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playPauseButton = !(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ? R.drawable.ic_baseline_pause_circle_outline_24 : R.drawable.ic_baseline_play_circle_outline_24;
        Log.d("MediaAudioService", "createNotification: CurrentMediaItem: " + mediaController.getMetadata().getDescription().getTitle());
        return new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.music_icon_big))
                .setContentTitle(mediaController.getMetadata().getDescription().getTitle())
                .setContentText(mediaController.getMetadata().getDescription().getDescription())
                .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", playPrevPendingIntent)
                .addAction(playPauseButton, "Play", playPausePendingIntent)
                .addAction(R.drawable.ic_baseline_skip_next_24, "Next", playNextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()))
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    public MediaSessionCompat getMediaSession(){
        return mediaSession;
    }

    public MediaControllerCompat getMediaController() {
        return mediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return transportControls;
    }

    void play() {
        transportControls.play();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void next(){
        transportControls.skipToNext();
        updateNotification();
    }
    void prev(){
        transportControls.skipToPrevious();
        updateNotification();
    }
    public void pausePlay(){
        if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
            transportControls.pause();
        else{
            transportControls.play();
        }
    }

    public long getCurrentDuration(){
        return mediaPlayer.getDuration();
    }
    public int getCurrentAudioIndex(){
        return mediaPlayer.getCurrentMediaItemIndex();
    }
    public long getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
//    void setCurrentAudioFromIndex(int index){
//        mediaPlayer.setCurrentAudioFromIndex(index);
//    }
//    void disableAutoplay(){
//        mediaPlayer.disableAutoplay();
//    }
//    void enableAutoplay(){
//        mediaPlayer.enableAutoplay();
//    }
    boolean isLooping(){
        return mediaPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE;
    }
    boolean isShuffle(){
        return mediaPlayer.getShuffleModeEnabled();
    }
    void switchShuffle(){
        mediaPlayer.setShuffleModeEnabled(!mediaPlayer.getShuffleModeEnabled());
    }
    void switchLooping(){
        if(isLooping())
            mediaPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        else
            mediaPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
    }
    void seekTo(long progress){
        mediaPlayer.seekTo(progress);
    }
//    MediaItemData getCurrentAudio(){
//        return mediaPlayer.getCurrentAudio();
//    }
    ArrayList<MediaItemData> getMediaItemsList(){
        return mediaItems;
    }
    ExoPlayer createMediaPlayer(ArrayList<MediaItemData> list){
//        mediaPlayer = MyMediaPlayer.createInstance(list);
        return mediaPlayer;
    }
    public void setSongList(ArrayList<MediaItemData> songList){
        mediaItems = songList;
        for (MediaItemData song: songList) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getPath())
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle(song.getTitle())
                            .setArtist(song.getAuthor())
                            .setArtworkData(song.getImageData(), MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                            .build())
                    .build();
            mediaPlayer.addMediaItem(mediaItem);
        }
    }

    public void seekToMediaItem(int index){
        mediaPlayer.seekTo(index, 0);
    }
    private void audioFocusChangeHandler(int focusChange){
        Log.d("XXX", "audioFocusChangeHandler: " + focusChange);
    }

    ArrayList<AudioChangedListener> audioChangedListenerList = new ArrayList<>();

    public interface AudioChangedListener{
        void onAudioChangedListener();
    }

    public void addOnAudioChangedListener(AudioChangedListener listener){
        audioChangedListenerList.add(listener);
    }

    public void notifyAudioChanged(){
        audioChangedListenerList.forEach(AudioChangedListener::onAudioChangedListener);
        updateNotification();
    }

    ArrayList<AudioStartedListener> audioStartedListenerList = new ArrayList<>();

    public interface AudioStartedListener{
        void onAudioStartedListener();
    }

    public void addOnAudioStartedListener(AudioStartedListener listener) {
        audioStartedListenerList.add(listener);
    }

    public void notifyAudioStarted(){
        audioStartedListenerList.forEach(AudioStartedListener::onAudioStartedListener);
        updateNotification();
    }
}