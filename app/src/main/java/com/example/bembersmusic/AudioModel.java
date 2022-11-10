package com.example.bembersmusic;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.Serializable;

public class AudioModel implements Serializable {
    private String path;
    private String title;
    private String duration;
    private Bitmap image;
    private String author;

    public AudioModel(String path, String title, String duration, String author) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
