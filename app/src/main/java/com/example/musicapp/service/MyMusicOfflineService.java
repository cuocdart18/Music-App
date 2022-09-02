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
import com.example.musicapp.thread.GetAllMusicThread;

import java.io.Serializable;
import java.util.List;

public class MyMusicOfflineService extends Service {
    public static final String SEND_TO_ACTIVITY = "send_data_to_activity";
    public static final String KEY_SEND_ACTION = "send_play_list";

    public static final String KEY_RECEIVE_ACTION = "receive_action";

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
        Bundle bundle = intent.getExtras();
        if (bundle != null) {

        }

        handleActionFromBroadcast(bundle);
        return START_STICKY;
    }

    private void handleActionFromBroadcast(Bundle bundle) {
        int action = bundle.getInt(KEY_RECEIVE_ACTION);

        switch (action) {
            case ACTION_PAUSE:
                break;
            case ACTION_RESUME:
                break;
            case ACTION_NEXT:
                break;
            case ACTION_PREV:
                break;
            case ACTION_STOP:
                break;
            case ACTION_START:
                break;
        }
    }

    // init for songs of service
    private void getAllMedia() {
        new GetAllMusicThread(MyMusicOfflineService.this, this.songs).start();
    }

}
