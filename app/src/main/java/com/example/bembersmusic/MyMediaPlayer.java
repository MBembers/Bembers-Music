package com.example.bembersmusic;

import android.media.MediaPlayer;

import java.util.ArrayList;

public class MyMediaPlayer extends MediaPlayer{
    static MyMediaPlayer instance;
    ArrayList<AudioModel> songsList;
    ArrayList<AudioModel> songsQuery;
    AudioModel currentSong;
    boolean shouldPlayNextOnEnded = false;

    public MyMediaPlayer(ArrayList<AudioModel> songsList, ArrayList<AudioModel> songsQuery, AudioModel currentSong, boolean shouldPlayNextOnEnded) {
        this.songsList = songsList;
        this.songsQuery = songsQuery;
        this.currentSong = currentSong;
        this.shouldPlayNextOnEnded = shouldPlayNextOnEnded;
    }

    public static MyMediaPlayer createInstance(ArrayList<AudioModel> songsList, ArrayList<AudioModel> songsQuery, AudioModel currentSong, boolean shouldPlayNextOnEnded){
        if(instance == null)
            return MyMediaPlayer(songsList, songsQuery, currentSong, shouldPlayNextOnEnded);
        return instance;
    }

    public static MyMediaPlayer getInstance(){
        return instance;
    }

    public static int currentIndex = -1;
}
