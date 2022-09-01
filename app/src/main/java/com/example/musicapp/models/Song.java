package com.example.musicapp.models;

public class Song {
    private long id;
    private String title;
    private String singer;

    public Song(long id, String title, String singer) {
        this.id = id;
        this.title = title;
        this.singer = singer;
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
}
