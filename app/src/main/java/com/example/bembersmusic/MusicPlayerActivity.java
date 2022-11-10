package com.example.bembersmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTextView,currentTimeTextView,totalTimeTextView;
    SeekBar seekBar;
    ImageView pausePlayBtn,nextBtn,previousBtn,musicIcon,shuffleBtn,repeatBtn,backBtn;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MyMediaPlayer myMediaPlayer = MyMediaPlayer.getInstance();
    boolean isTrackingSeekBarTouch = false;

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

        if(myMediaPlayer != null){
            songsList = myMediaPlayer.getAudiosList();

            if(!myMediaPlayer.isPlaying())
                myMediaPlayer.playCurrentAudio();
            setUpCurrentSongAndData();
            myMediaPlayer.addOnAudioChangedListener(this::setUpCurrentSongAndData);
        }

//        Thread thread = new Thread(() -> {
//            try {
//                while(true) {
//                if(myMediaPlayer !=null){
//                    if(!isTrackingSeekBarTouch)
//                        seekBar.setProgress(myMediaPlayer.getCurrentPosition());
//                    titleTextView.setSelected(true);
//
//                    currentTimeTextView.setText(convertToMMSS(myMediaPlayer.getCurrentPosition()+""));
//
//                    if(myMediaPlayer.isPlaying()){
//                        pausePlayBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
//                    }else{
//                        pausePlayBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
//                    }
//                }
//                Thread.sleep(100);
//            }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        thread.start();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(myMediaPlayer !=null){
                    if(!isTrackingSeekBarTouch)
                        seekBar.setProgress(myMediaPlayer.getCurrentPosition());
                    titleTextView.setSelected(true);
                    currentTimeTextView.setText(convertToMMSS(myMediaPlayer.getCurrentPosition()+""));

                    if(myMediaPlayer.isPlaying()){
                        pausePlayBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }else{
                        pausePlayBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    }
                }
                new Handler().postDelayed(this,100);
            }
        });

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
                if(myMediaPlayer !=null){
                    myMediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        setupButtons();
    }

    private void setUpCurrentSongAndData(){
        currentSong = myMediaPlayer.currentAudio;
        if(currentSong.getImage() == null)
            musicIcon.setImageDrawable(AppCompatResources.getDrawable(
                    this, R.drawable.music_icon_big));
            else
            musicIcon.setImageBitmap(currentSong.getImage());

        titleTextView.setText(currentSong.getTitle());
        totalTimeTextView.setText(convertToMMSS(currentSong.getDuration()));
        seekBar.setProgress(0);
        seekBar.setMax(myMediaPlayer.getDuration());
        repeatBtn.setColorFilter(myMediaPlayer.isRepeat() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupButtons(){
        pausePlayBtn.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());
        shuffleBtn.setOnClickListener(v -> switchShuffle());
        repeatBtn.setOnClickListener(v -> switchLooping());
        backBtn.setOnClickListener(v -> finish());
    }

    private void switchShuffle() {
        myMediaPlayer.switchShuffle();
        shuffleBtn.setColorFilter(myMediaPlayer.isShuffle() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void switchLooping(){
        myMediaPlayer.switchLooping();
        repeatBtn.setColorFilter(myMediaPlayer.isRepeat() ? getColor(R.color.orange) :
                getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void pausePlay(){
        myMediaPlayer.pausePlay();
    }

    private void playNextSong() {
        myMediaPlayer.stepToNextAudio();
    }

    private void playPreviousSong() {
        myMediaPlayer.stepToPreviousAudio();
    }

    public static String convertToMMSS(String duration){
        long millis = Long.parseLong(duration);
        return String.format(Locale.ENGLISH, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}