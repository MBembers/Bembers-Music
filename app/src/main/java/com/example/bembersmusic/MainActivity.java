package com.example.bembersmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView, songBarTitleTextView, songBarAuthorTextView;
    ImageView songBarIcon;
    ImageButton songBarPlayBtn;
    RelativeLayout songBarLayout;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    MyMediaPlayer myMediaPlayer;

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
            AudioModel songData = new AudioModel(cursor.getString(1),cursor.getString(0),cursor.getString(2), author);
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

        if(songsList.size()==0){
            noMusicTextView.setVisibility(View.VISIBLE);
        }else{
            //recyclerview
            myMediaPlayer = MyMediaPlayer.createInstance(songsList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(songsList,getApplicationContext()));
        }



        songBarLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        myMediaPlayer.addOnAudioChangedListener(this::updateSongBar);
        myMediaPlayer.addOnAudioChangedListener(this::updateRecycleView);
        myMediaPlayer.setOnAudioStartedListener(this::updateSongBar);

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(myMediaPlayer !=null){
                    if(myMediaPlayer.isPlaying()){
                        songBarPlayBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }else{
                        songBarPlayBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    }
                }
                new Handler().postDelayed(this,100);
            }
        });

        songBarPlayBtn.setOnClickListener(v -> myMediaPlayer.pausePlay());

    }

    public void updateSongBar(){
        Log.d("Handler", "MainActivityHandler: ");
        recyclerView.refreshDrawableState();
        songBarLayout.setVisibility(View.VISIBLE);
        MyMediaPlayer myMediaPlayer = MyMediaPlayer.getInstance();
        songBarTitleTextView.setText(myMediaPlayer.getCurrentAudio().getTitle());
        songBarAuthorTextView.setText(myMediaPlayer.getCurrentAudio().getAuthor());
        if(myMediaPlayer.getCurrentAudio().getImage() != null)
            songBarIcon.setImageBitmap(myMediaPlayer.getCurrentAudio().getImage());
        else songBarIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.music_icon));
    }

    private void updateRecycleView(){
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
        if(recyclerView!=null){
            recyclerView.setAdapter(new MusicListAdapter(songsList,getApplicationContext()));
        }
    }
}