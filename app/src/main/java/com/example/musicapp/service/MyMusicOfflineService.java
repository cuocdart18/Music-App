package com.example.musicapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.activity.OfflineModeActivity;
import com.example.musicapp.application.MyApplication;
import com.example.musicapp.broadcast_receiver.PlayMusicOfflineBroadcast;
import com.example.musicapp.models.Song;
import com.example.musicapp.thread.GetAllMusicThread;

import java.io.Serializable;
import java.util.List;

public class MyMusicOfflineService extends Service {
    public static final String SEND_TO_ACTIVITY = "send_data_to_activity";

    public static final String KEY_SEND_ACTION = "send_action";
    public static final String KEY_RECEIVE_ACTION = "receive_action";

    public static final int ACTION_PAUSE = 1001;
    public static final int ACTION_RESUME = 1002;
    public static final int ACTION_NEXT = 1003;
    public static final int ACTION_PREV = 1004;
    public static final int ACTION_STOP = 1005;
    public static final int ACTION_START = 1006;
    public static final int ACTION_SHUFFLE = 1007;
    public static final int ACTION_LOOP = 1008;

    private Song song;
    private List<Song> songs;
    private boolean isPlaying;
    private MediaPlayer media;
    private NotificationCompat.Builder notiBuilder;

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

        handleActionFromBroadcast(intent);
        return START_STICKY;
    }

    private void handleActionFromBroadcast(Intent intent) {
        int action = intent.getIntExtra(KEY_RECEIVE_ACTION, 0);

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
            case ACTION_SHUFFLE:
                break;
            case ACTION_LOOP:
                break;
        }
    }

    // send notification for start music
    private void sendNotificationMediaStyle() {
        Intent intentContext = new Intent(MyMusicOfflineService.this, OfflineModeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyMusicOfflineService.this,
                0, intentContext, PendingIntent.FLAG_UPDATE_CURRENT);
        //TODO: tim hieu them ve MediaSessionCompat
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

        if (song != null) {
            notiBuilder = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                    // Show controls on lock screen even when user hides sensitive content
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    // Apply the media style template
                    .setSmallIcon(R.drawable.ic_audio_file)
                    .setSound(null)
                    .setContentIntent(pendingIntent)
                    .setSubText(song.getTitle())
                    .setContentTitle(song.getTitle())
                    .setContentText(song.getSinger());
        }

        if (isPlaying) {
            notiBuilder
                    .clearActions()
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this, ACTION_PREV))
                    .addAction(R.drawable.ic_play_arrow_24, "Pause", getPendingIntent(this, ACTION_PAUSE))
                    .addAction(R.drawable.ic_skip_next_24, "Next", getPendingIntent(this, ACTION_NEXT))
                    .addAction(R.drawable.ic_stop_24, "Stop", getPendingIntent(this, ACTION_STOP));
        } else {
            notiBuilder
                    .clearActions()
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this, ACTION_PREV))
                    .addAction(R.drawable.ic_pause_24, "Play", getPendingIntent(this, ACTION_RESUME))
                    .addAction(R.drawable.ic_skip_next_24, "Next", getPendingIntent(this, ACTION_NEXT))
                    .addAction(R.drawable.ic_stop_24, "Stop", getPendingIntent(this, ACTION_STOP));
        }

        notiBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2) // btn Previous, Next, Pause in Collapsed
                .setMediaSession(mediaSessionCompat.getSessionToken()));

        Notification notification = notiBuilder.build();

        startForeground(1, notification);
    }

    // send data by pendingIntent to broadcast receiver from Notification (to service)
    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, PlayMusicOfflineBroadcast.class);
        intent.putExtra(KEY_SEND_ACTION, action);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // send action to fragment
    private void sendActionToFragment(int action) {
        Intent intentSend = new Intent(SEND_TO_ACTIVITY);
        // put data, something

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    // init for songs of service
    private void getAllMedia() {
        new GetAllMusicThread(MyMusicOfflineService.this, this.songs).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (media != null) {
            media.release();
            media = null;
        }
    }
}
