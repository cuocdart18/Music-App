package com.example.musicapp.thread;

import android.content.Context;

import com.example.musicapp.AppUtils;
import com.example.musicapp.models.Song;

import java.util.List;

public class GetAllMusicThread extends Thread {
    private List<Song> songs;
    private Context context;

    public GetAllMusicThread(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public void run() {
        songs = AppUtils.getInstance(context).getAllMediaMp3Files();
    }
}
