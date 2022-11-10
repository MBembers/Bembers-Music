package com.example.bembersmusic;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MyMediaPlayer extends MediaPlayer{
    static MyMediaPlayer instance;
    ArrayList<AudioModel> audiosList;
    LinkedList<AudioModel> audiosQueue;
    AudioModel currentAudio;
    boolean autoplay = false;
    private static int currentIndex = -1;
    private boolean shuffle;

    private final ArrayList<AudioChangedListener> audioChangedListenerList = new ArrayList<>();
    private AudioStartedListener audioStartedListener;

    public MyMediaPlayer(ArrayList<AudioModel> audiosList) {
        super();
        this.audiosList = audiosList;
        this.currentAudio = audiosList.get(0);
        this.autoplay = false;
        setOnCompletionListener(mediaPlayer -> completionHandler());
    }

    public static MyMediaPlayer createInstance(ArrayList<AudioModel> audiosList){
        if(instance == null){
            instance = new MyMediaPlayer(audiosList);
        }
        Log.d("MyMediaPlayer", "createInstance: Created MyMediaPlayer");
        return instance;
    }

    public static MyMediaPlayer getInstance(){
        return instance;
    }

    public void playCurrentAudio(){
        reset();
        try {
            setDataSource(currentAudio.getPath());
            prepare();
            start();
            enableAutoplay();
            notifyAudioStarted();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentAudioFromIndex(int index){
        currentIndex = index;
        setCurrentAudioFromCurrentIndex();
    }

    public void setCurrentAudioFromCurrentIndex(){
        currentAudio = audiosList.get(currentIndex);
    }

    public void stepToNextAudio(){
        if(currentIndex == audiosList.size() - 1)
            return;
        if(shuffle){
            Random random = new Random();
            currentIndex = random.nextInt(audiosList.size());
        }
        else{
            currentIndex += 1;
        }
        reset();
        setCurrentAudioFromCurrentIndex();
        playCurrentAudio();
        notifyAudioChanged();
    }

    public void stepToNextAudio(boolean playNext){
        if(currentIndex == audiosList.size() - 1)
            return;
        if(shuffle){
            Random random = new Random();
            currentIndex = random.nextInt(audiosList.size());
        }
        else{
            currentIndex += 1;
        }
        reset();
        setCurrentAudioFromCurrentIndex();
        if (playNext)
            playCurrentAudio();
        notifyAudioChanged();
    }

    public void stepToPreviousAudio(){
        if(getCurrentPosition() >= 2000 || currentIndex == 0){
            seekTo(0);
            return;
        }
        if(shuffle){
            Random random = new Random();
            currentIndex = random.nextInt(audiosList.size());
        }
        else{
            currentIndex -= 1;
        }
        reset();
        setCurrentAudioFromCurrentIndex();
        playCurrentAudio();
        notifyAudioChanged();
    }

    public void stepToPreviousAudio(boolean playNext){
        if(getCurrentPosition() >= 2000 || currentIndex == 0){
            seekTo(0);
            return;
        }
        if(shuffle){
            Random random = new Random();
            currentIndex = random.nextInt(audiosList.size());
        }
        else{
            currentIndex -= 1;
        }
        reset();
        setCurrentAudioFromCurrentIndex();
        if (playNext)
            playCurrentAudio();
        notifyAudioChanged();
    }

    public void pausePlay(){
        if(isPlaying())
            pause();
        else
            start();
    }

    public void switchLooping(){
        setLooping(!isLooping());
    }

    public void switchShuffle(){
        setShuffle(!shuffle);
//        if(shuffle){
//            Collections.shuffle(audiosQueue);
//        }
    }

    public void createQueueFromList(){
//        audiosQueue = (LinkedList<AudioModel>) audiosList.subList(currentIndex, audiosList.size());
    }

    public void getNextAudioFromQueue(){
        currentAudio = audiosQueue.pollFirst();
    }

    private void completionHandler() {
        Log.d("AudioCompletion", "completionHandler: Audio Completed");
        if (autoplay){
            stepToNextAudio();
        }
    }

    public interface AudioChangedListener {
        void onAudioChangedListener();
    }

    public void addOnAudioChangedListener(AudioChangedListener audioChangedListener){
        this.audioChangedListenerList.add(audioChangedListener);
    }

    private void notifyAudioChanged(){
        for (AudioChangedListener audioChangedListener : audioChangedListenerList){

            audioChangedListener.onAudioChangedListener();
        }
    }

    public interface AudioStartedListener {
        void onAudioStartedListener();
    }

    public void setOnAudioStartedListener(AudioStartedListener audioStartedListener){
        this.audioStartedListener = audioStartedListener;
    }

    public void notifyAudioStarted(){
        if(audioStartedListener != null)
            audioStartedListener.onAudioStartedListener();
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isRepeat(){
        return isLooping();
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public ArrayList<AudioModel> getAudiosList() {
        return audiosList;
    }

    public void setAudiosList(ArrayList<AudioModel> audiosList) {
        this.audiosList = audiosList;
    }

    public LinkedList<AudioModel> getAudiosQueue() {
        return audiosQueue;
    }

    public void setAudiosQueue(LinkedList<AudioModel> audiosQueue) {
        this.audiosQueue = audiosQueue;
    }

    public AudioModel getCurrentAudio() {
        return currentAudio;
    }

    public void setCurrentAudio(AudioModel currentAudio) {
        this.currentAudio = currentAudio;
    }

    public boolean isAutoplay() {
        return autoplay;
    }

    public void enableAutoplay(){
        setAutoplay(true);
    }

    public void disableAutoplay(){
        setAutoplay(false);
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void setCurrentIndex(int currentIndex) {
        MyMediaPlayer.currentIndex = currentIndex;
    }

}
