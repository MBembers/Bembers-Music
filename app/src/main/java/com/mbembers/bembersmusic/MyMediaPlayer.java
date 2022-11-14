//package com.mbembers.bembersmusic;
//
//import android.media.MediaPlayer;
//import android.util.Log;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.Random;
//
//public class MyMediaPlayer extends MediaPlayer{
//    static MyMediaPlayer instance;
//    ArrayList<MediaItemData> audiosList;
//    LinkedList<MediaItemData> audiosQueue;
//    MediaItemData currentAudio;
//    boolean autoplay;
//    private static int currentIndex = -1;
//    private boolean shuffle;
//
//    private final ArrayList<AudioChangedListener> audioChangedListenerList = new ArrayList<>();
//    private AudioStartedListener audioStartedListener;
//
//    public MyMediaPlayer(ArrayList<MediaItemData> audiosList) {
//        super();
//        this.audiosQueue = new LinkedList<>();
//        this.audiosList = audiosList;
//        this.currentAudio = audiosList.get(0);
//        this.autoplay = false;
//        setOnCompletionListener(mediaPlayer -> completionHandler());
//    }
//
//    public static MyMediaPlayer createInstance(ArrayList<MediaItemData> audiosList){
//        if(instance == null){
//            instance = new MyMediaPlayer(audiosList);
//        }
//        Log.d("MyMediaPlayer", "createInstance: CREATED MyMediaPlayer");
//        return instance;
//    }
//
//    public static MyMediaPlayer getInstance(){
//        return instance;
//    }
//
//    public void playCurrentAudio(){
//        reset();
//        try {
//            setDataSource(currentAudio.getPath());
//            prepare();
//            start();
//            enableAutoplay();
//            notifyAudioStarted();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void prepareCurrentAudio(){
//        try {
//            setDataSource(currentAudio.getPath());
//            prepare();
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
//        }
//
//    public void setCurrentAudioFromIndex(int index){
//        currentIndex = index;
//        setCurrentAudioFromCurrentIndex();
//    }
//
//    public void setCurrentAudioFromCurrentIndex(){
//        currentAudio = audiosList.get(currentIndex);
//    }
//
//    public void stepToNextAudio(boolean playNext){
//        if(currentIndex == audiosList.size() - 1)
//            return;
//        if(shuffle && audiosQueue.isEmpty()){
//            Random random = new Random();
//            currentIndex = random.nextInt(audiosList.size());
//        }
//        else if(!audiosQueue.isEmpty()){
//            setNextAudioFromQueue();
//        }
//        else{
//            currentIndex += 1;
//            setCurrentAudioFromCurrentIndex();
//        }
//        reset();
//        if(playNext)
//            playCurrentAudio();
//        notifyAudioChanged();
//    }
//
//    public void stepToPreviousAudio(boolean playPrev){
//        if(getCurrentPosition() >= 2000 || currentIndex == 0){
//            seekTo(0);
//            return;
//        }
//        if(shuffle){
//            Random random = new Random();
//            currentIndex = random.nextInt(audiosList.size());
//        }
//        else{
//            currentIndex -= 1;
//        }
//        reset();
//        setCurrentAudioFromCurrentIndex();
//        if (playPrev)
//            playCurrentAudio();
//        notifyAudioChanged();
//    }
//
//    public void pausePlay(){
//        if(isPlaying())
//            pause();
//        else
//            start();
//    }
//
//    public void switchLooping(){
//        setLooping(!isLooping());
//    }
//
//    public void switchShuffle(){
//        setShuffle(!shuffle);
////        if(shuffle){
////            Collections.shuffle(audiosQueue);
////        }
//    }
//
//    public void createQueueFromList(){
////        audiosQueue = (LinkedList<AudioModel>) audiosList.subList(currentIndex, audiosList.size());
//    }
//
//    public void setNextAudioFromQueue(){
//        currentAudio = audiosQueue.pollFirst();
//    }
//
//    private void completionHandler() {
//        Log.d("AudioCompletion", "completionHandler: Audio Completed");
//        if (autoplay){
//            stepToNextAudio(true);
//        }
//    }
//
//    public interface AudioChangedListener {
//        void onAudioChangedListener();
//    }
//
//    public void addOnAudioChangedListener(AudioChangedListener audioChangedListener){
//        this.audioChangedListenerList.add(audioChangedListener);
//    }
//
//    private void notifyAudioChanged(){
//        for (AudioChangedListener audioChangedListener : audioChangedListenerList){
//
//            audioChangedListener.onAudioChangedListener();
//        }
//    }
//
//    public interface AudioStartedListener {
//        void onAudioStartedListener();
//    }
//
//    public void setOnAudioStartedListener(AudioStartedListener audioStartedListener){
//        this.audioStartedListener = audioStartedListener;
//    }
//
//    public void notifyAudioStarted(){
//        if(audioStartedListener != null)
//            audioStartedListener.onAudioStartedListener();
//    }
//
//    public boolean isShuffle() {
//        return shuffle;
//    }
//
//    public void setShuffle(boolean shuffle) {
//        this.shuffle = shuffle;
//    }
//
//    public ArrayList<MediaItemData> getAudiosList() {
//        return audiosList;
//    }
//
//    public void setAudiosList(ArrayList<MediaItemData> audiosList) {
//        this.audiosList = audiosList;
//    }
//
//    public LinkedList<MediaItemData> getAudiosQueue() {
//        return audiosQueue;
//    }
//
//    public void setAudiosQueue(LinkedList<MediaItemData> audiosQueue) {
//        this.audiosQueue = audiosQueue;
//    }
//
//    public void addAudioToQueue(MediaItemData mediaItem){
//        audiosQueue.push(mediaItem);
//    }
//
//    public void clearAudioQueue(){
//        audiosQueue = new LinkedList<>();
//    }
//
//    public boolean isAudioQueueEmpty(){
//        return audiosQueue.isEmpty();
//    }
//
//    public MediaItemData getCurrentAudio() {
//        return currentAudio;
//    }
//
//    public void setCurrentAudio(MediaItemData currentAudio) {
//        this.currentAudio = currentAudio;
//    }
//
//    public boolean isAutoplay() {
//        return autoplay;
//    }
//
//    public void enableAutoplay(){
//        setAutoplay(true);
//    }
//
//    public void disableAutoplay(){
//        setAutoplay(false);
//    }
//
//    public void setAutoplay(boolean autoplay) {
//        this.autoplay = autoplay;
//    }
//
//    public static int getCurrentIndex() {
//        return currentIndex;
//    }
//
//    public static void setCurrentIndex(int currentIndex) {
//        MyMediaPlayer.currentIndex = currentIndex;
//    }
//
//}
