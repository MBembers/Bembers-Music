package com.mbembers.bembersmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    RecyclerView recyclerView;
    TextView noMusicTextView, songBarTitleTextView, songBarAuthorTextView;
    ImageView songBarIcon;
    ImageButton songBarPlayBtn;
    RelativeLayout songBarLayout;
    ArrayList<MediaItemData> songsList = new ArrayList<>();
    MyMediaPlayer myMediaPlayer;
    private MediaAudioService mediaAudioService;
    private boolean boundToService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        songBarLayout = findViewById(R.id.song_bar_layout);
        songBarTitleTextView = findViewById(R.id.song_bar_title_text);
        songBarAuthorTextView = findViewById(R.id.song_bar_author_text);
        songBarPlayBtn = findViewById(R.id.song_bar_play);
        songBarIcon = findViewById(R.id.song_bar_icon);

        if(checkPermission() == false){
            requestPermission();
            return;
        }

        setup();
    }

    private void setup(){
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.AUTHOR
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC +" != 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,null,null);
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        while(cursor.moveToNext()){
            String author = cursor.getString(3);
            if(author == null){
                author = "Unknown artist";
            }
            MediaItemData songData = new MediaItemData(cursor.getString(1),cursor.getString(0),cursor.getString(2), author);
            if(new File(songData.getPath()).exists()){
                mmr.setDataSource(songData.getPath());
                byte [] data = mmr.getEmbeddedPicture();
                if(data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    songData.setImage(bitmap);
                }
                songsList.add(songData);
            }
        }

        doBindService();
    }

    public void updateSongBar(){
        Log.d("Handler", "MainActivityHandler: ");
        recyclerView.refreshDrawableState();
        songBarLayout.setVisibility(View.VISIBLE);
        songBarTitleTextView.setText(mediaAudioService.getCurrentAudio().getTitle());
        songBarAuthorTextView.setText(mediaAudioService.getCurrentAudio().getAuthor());
        if(mediaAudioService.getCurrentAudio().getImage() != null)
            songBarIcon.setImageBitmap(mediaAudioService.getCurrentAudio().getImage());
        else songBarIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.music_icon));
    }

    private void updateRecycleView(){
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this,"Without read storage permission application won't work properly.",Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},77);

        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.FOREGROUND_SERVICE)){
            Toast.makeText(MainActivity.this,"Without foreground permission application won't work properly.",Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.FOREGROUND_SERVICE},727);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 77:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        setup();
                } else {
                    requestPermission();
                }
                return;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume: mediaAudioService = " + mediaAudioService);
        if(recyclerView != null){
            MusicListAdapter listAdapter = new MusicListAdapter(songsList, getApplicationContext());
            listAdapter.setOnItemClickedListener(this::musicListAdapterClickHandler);
            recyclerView.setAdapter(listAdapter);
        }
        if(mediaAudioService != null){
            return;
        }
        doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause: mediaAudioService = " + mediaAudioService);
        if(mediaAudioService != null && boundToService)
            doUnbindService();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MediaAudioService.MyBinder myBinder = (MediaAudioService.MyBinder) iBinder;
        mediaAudioService = myBinder.getService();
        myMediaPlayer = mediaAudioService.createMediaPlayer(songsList);
        myMediaPlayer.prepareCurrentAudio();

        if(songsList.size()==0){
            noMusicTextView.setVisibility(View.VISIBLE);
        }else{
            //recyclerview
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            MusicListAdapter listAdapter = new MusicListAdapter(songsList, getApplicationContext());
            listAdapter.setOnItemClickedListener(this::musicListAdapterClickHandler);
            recyclerView.setAdapter(listAdapter);
        }

        songBarLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MediaPlayerActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        myMediaPlayer.addOnAudioChangedListener(this::updateSongBar);
        myMediaPlayer.addOnAudioChangedListener(this::updateRecycleView);
        myMediaPlayer.setOnAudioStartedListener(this::updateSongBar);

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (myMediaPlayer != null) {
                        if (myMediaPlayer.isPlaying()) {
                            songBarPlayBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        } else {
                            songBarPlayBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        }
                    }
                }
                catch (IllegalStateException e){
                    e.printStackTrace();
                }
                new Handler().postDelayed(this,100);
            }
        });

        songBarPlayBtn.setOnClickListener(v -> myMediaPlayer.pausePlay());

        Log.d("MainActivity", "onServiceConnected: mediaAudioService = " + mediaAudioService);
        updateSongBar();
        Intent intent = new Intent(this, MediaAudioService.class); // Build the intent for the service
        startForegroundService(intent);
    }

    void musicListAdapterClickHandler(MusicListAdapter.ViewHolder holder){
        Log.d("AdapterClick", "musicListAdapterClickHandler: " + holder.getAdapterPosition());
        mediaAudioService.setCurrentAudioFromIndex(holder.getAdapterPosition());
        mediaAudioService.disableAutoplay();
        mediaAudioService.playCurrentMedia();
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mediaAudioService = null;
        Log.d("MainActivity", "onServiceDisconnected: mediaAudioService = " + mediaAudioService);
    }

    public void doBindService() {
        bindService(new Intent(this, MediaAudioService.class), this, BIND_AUTO_CREATE);
        boundToService = true;
    }

    public void doUnbindService() {
        unbindService(this);
        boundToService = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy: MAIN ACTIVITY DESTROYED");
    }
}