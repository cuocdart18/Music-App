package com.example.musicapp.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicapp.AppUtils;
import com.example.musicapp.models.Song;

import java.io.Serializable;
import java.util.List;

public class MyMusicOfflineService extends Service {
    public static final String SEND_TO_ACTIVITY = "send_data_to_activity";
    public static final String KEY_SEND_ACTION = "send_play_list";

    public static final String KEY_PLAYLIST_SONG = "play_list_song";
    public static final int ACTION_SEND_PLAYLIST = 999;

    public static final int ACTION_PAUSE = 1001;
    public static final int ACTION_RESUME = 1002;
    public static final int ACTION_NEXT = 1003;
    public static final int ACTION_PREV = 1004;
    public static final int ACTION_STOP = 1005;
    public static final int ACTION_START = 1006;

    private List<Song> songs;
    private MediaPlayer media;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // read all media file by worker thread
        getAllMedia();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllMedia() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                songs = AppUtils.getInstance(MyMusicOfflineService.this).getAllMediaMp3Files();
            }
        });
        thread.start();
    }
}
