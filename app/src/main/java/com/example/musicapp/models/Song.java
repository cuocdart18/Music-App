package com.example.musicapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Serializable{
    private long id;
    private String title;
    private String singer;
    private boolean isPlaying;
    private int posInList;
    private double startTime = 0;
    private double finalTime = 0;

    public Song(long id, String title, String singer, int posInList) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.posInList = posInList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getPosInList() {
        return posInList;
    }

    public void setPosInList(int posInList) {
        this.posInList = posInList;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinalTime() {
        return finalTime;
    }

    public void setFinalTime(double finalTime) {
        this.finalTime = finalTime;
    }
}
