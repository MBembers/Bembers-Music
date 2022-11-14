package com.mbembers.bembersmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MediaPlayerActivity extends AppCompatActivity implements ServiceConnection, MediaAction  {

    private TextView titleTextView,currentTimeTextView,totalTimeTextView;
    private SeekBar seekBar;
    private ImageView pausePlayBtn,nextBtn,previousBtn,musicIcon,shuffleBtn,repeatBtn,backBtn;
    private ArrayList<MediaItemData> songsList;
//    private MediaItemData currentSong;
    private MediaAudioService mediaAudioService;
    boolean isTrackingSeekBarTouch = false;
    private boolean boundToService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        Log.d("MusicPlayer", "onCreate: ");

        titleTextView = findViewById(R.id.song_title);
        currentTimeTextView = findViewById(R.id.current_time);
        totalTimeTextView = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlayBtn = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        shuffleBtn = findViewById(R.id.shuffle);
        repeatBtn = findViewById(R.id.repeat);
        backBtn = findViewById(R.id.back);

        titleTextView.setSelected(true);

        doBindService();
    }


    private void setupUi(){
        MediaPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(mediaAudioService != null){
                        setUpCurrentSongAndData();
                        if(!isTrackingSeekBarTouch)
                            seekBar.setProgress((int) mediaAudioService.getCurrentPosition());
                        currentTimeTextView.setText(convertToMMSS(mediaAudioService.getCurrentPosition()+""));

                        if(mediaAudioService.isPlaying()){
                            pausePlayBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        }else{
                            pausePlayBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        }
                    }
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }

                new Handler().postDelayed(this,100);
            }
        });

        setupSeekbar();
        setupButtons();
    }

    private void setUpCurrentSongAndData(){
        MediaMetadataCompat metadata = mediaAudioService.getMediaController().getMetadata();
        if(metadata.getDescription().getIconBitmap() == null)
            musicIcon.setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.music_icon_big));
            else
            musicIcon.setImageBitmap(metadata.getDescription().getIconBitmap());

        titleTextView.setText(metadata.getDescription().getTitle());
        totalTimeTextView.setText(convertToMMSS(mediaAudioService.getCurrentDuration()));
        seekBar.setProgress(0);
        seekBar.setMax((int) mediaAudioService.getCurrentDuration());
        repeatBtn.setColorFilter(mediaAudioService.isLooping() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupButtons(){
        pausePlayBtn.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNext());
        previousBtn.setOnClickListener(v-> playPrev());
        shuffleBtn.setOnClickListener(v -> switchShuffle());
        repeatBtn.setOnClickListener(v -> switchLooping());
        backBtn.setOnClickListener(v -> finish());
        musicIcon.setOnClickListener(v -> test());
    }

    private void test() {
        Log.d("MediaPlayerActivity", "sussy: isLooping: " + mediaAudioService.isLooping() + " isShuffle: " + mediaAudioService.isShuffle());
    }

    private void switchShuffle() {
        mediaAudioService.switchShuffle();
        shuffleBtn.setColorFilter(mediaAudioService.isShuffle() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void switchLooping(){
        mediaAudioService.switchLooping();
        repeatBtn.setColorFilter(mediaAudioService.isLooping() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void playNext() {
        mediaAudioService.next();
    }

    @Override
    public void playPrev() {
        mediaAudioService.prev();
    }
    @Override
    public void pausePlay(){
        mediaAudioService.pausePlay();
    }

    public void setupSeekbar(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingSeekBarTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingSeekBarTouch = false;
                if(mediaAudioService !=null){
                    mediaAudioService.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MediaAudioService.MyBinder myBinder = (MediaAudioService.MyBinder) iBinder;
        mediaAudioService = myBinder.getService();
        Log.d("XXX", "onServiceConnected: CONNECTED");
        setupUi();
        setUpCurrentSongAndData();
        mediaAudioService.addOnAudioChangedListener(this::setUpCurrentSongAndData);
        Intent intent = new Intent(this, MediaAudioService.class); // Build the intent for the service
        startForegroundService(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mediaAudioService = null;
    }

    @Override
    protected void onResume() {
        if(mediaAudioService != null){
            super.onResume();
            return;
        }
        doBindService();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaAudioService != null && boundToService)
            doUnbindService();
    }

    public static String convertToMMSS(String duration){
        long millis = Long.parseLong(duration);
        return String.format(Locale.ENGLISH, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static String convertToMMSS(long duration){
        return String.format(Locale.ENGLISH, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
    }

    public void doBindService() {
        bindService(new Intent(this, MediaAudioService.class), this, BIND_AUTO_CREATE);
        boundToService = true;
    }

    public void doUnbindService() {
        unbindService(this);
        boundToService = false;
    }
}