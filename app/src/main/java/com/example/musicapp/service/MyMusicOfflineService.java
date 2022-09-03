package com.example.musicapp.service;

import static com.example.musicapp.AppUtils.ACTION_LOOP;
import static com.example.musicapp.AppUtils.ACTION_MUSIC;
import static com.example.musicapp.AppUtils.ACTION_NEXT;
import static com.example.musicapp.AppUtils.ACTION_PAUSE;
import static com.example.musicapp.AppUtils.ACTION_PREV;
import static com.example.musicapp.AppUtils.ACTION_RESUME;
import static com.example.musicapp.AppUtils.ACTION_SEEK;
import static com.example.musicapp.AppUtils.ACTION_SHUFFLE;
import static com.example.musicapp.AppUtils.ACTION_START;
import static com.example.musicapp.AppUtils.ACTION_STOP;
import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;
import static com.example.musicapp.AppUtils.KEY_SEND_ACTION;
import static com.example.musicapp.AppUtils.OBJ_SONG;
import static com.example.musicapp.AppUtils.POSITION;
import static com.example.musicapp.AppUtils.SEND_LIST_SONG;
import static com.example.musicapp.AppUtils.SEND_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.STATUS_PLAYING;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class MyMusicOfflineService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private long currentSong = -1;
    private int positionSong = -1;
    private double startTime = 0;
    private double finalTime = 0;
    private boolean isPlaying;
    private boolean isLoop;

    private Song currentObjSong;
    private List<Song> songs;
    private MediaPlayer media;
    private NotificationCompat.Builder notiBuilder;

    // handler update time
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startTime = media.getCurrentPosition();
            handler.postDelayed(runnable, 1000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*// read all media file by worker thread
        getAllMedia();*/

        // init media
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // receive songs by context
        if (this.songs == null) {
            this.songs = (List<Song>) intent.getSerializableExtra(SEND_LIST_SONG);
        }

        Bundle bundle = intent.getExtras();
        if (bundle != null) {

        }

        handleActionFromBroadcast(intent);
        return START_NOT_STICKY;
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
                handleActionStop(intent);
                break;
            case ACTION_START:
                hanleActionStart(intent);
                break;
            case ACTION_SHUFFLE:
                break;
            case ACTION_LOOP:
                break;
            case ACTION_SEEK:
                break;
        }
    }

    private void hanleActionStart(Intent intent) {
        Bundle bundle = intent.getExtras();
        positionSong = bundle.getInt(POSITION, 0);
        currentObjSong = songs.get(positionSong);
        playMusic(currentObjSong);
    }

    private void handleActionStop(Intent intent) {
        stopSelf();
        sendActionToActivity(ACTION_STOP);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        // get time
        startTime = mp.getCurrentPosition();
        finalTime = mp.getDuration();

        // send time for fragment


        handler.postDelayed(runnable, 1000);
        isPlaying = true;
        // update notification
        sendNotificationMediaStyle();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isLoop) {
            mp.start();
        }
        nextMusic();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    private void playMusic(Song currentObjSong) {
        media.reset();
        //get id
        currentSong = currentObjSong.getId();
        //set Uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        //setting this uri as data source
        try {
            media.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        //complete the play song
        media.prepareAsync();
    }

    private void pauseMusic() {
        media.pause();
        isPlaying = false;
    }

    private void nextMusic() {
        if (positionSong == songs.size() - 1) {
            positionSong = 0;
        } else {
            positionSong++;
        }
        Song song = songs.get(positionSong);
        playMusic(song);
    }

    private void prevMusic() {
        if (positionSong == 0) {
            positionSong = songs.size() - 1;
        } else {
            positionSong--;
        }
        Song song = songs.get(positionSong);
        playMusic(song);
    }

    // send notification for start music
    private void sendNotificationMediaStyle() {
        Intent intentContext = new Intent(MyMusicOfflineService.this, OfflineModeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyMusicOfflineService.this,
                0, intentContext, PendingIntent.FLAG_UPDATE_CURRENT);
        //TODO: tim hieu them ve MediaSessionCompat
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

        if (currentObjSong != null) {
            notiBuilder = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                    // Show controls on lock screen even when user hides sensitive content
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    // Apply the media style template
                    .setSmallIcon(R.drawable.ic_audio_file)
                    .setSound(null)
                    .setContentIntent(pendingIntent)
                    .setSubText(currentObjSong.getTitle())
                    .setContentTitle(currentObjSong.getTitle())
                    .setContentText(currentObjSong.getSinger());
        }

        if (isPlaying) {
            notiBuilder
                    .clearActions()
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this, ACTION_PREV))
                    .addAction(R.drawable.ic_pause_24, "Pause", getPendingIntent(this, ACTION_PAUSE))
                    .addAction(R.drawable.ic_skip_next_24, "Next", getPendingIntent(this, ACTION_NEXT))
                    .addAction(R.drawable.ic_stop_24, "Stop", getPendingIntent(this, ACTION_STOP));
        } else {
            notiBuilder
                    .clearActions()
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this, ACTION_PREV))
                    .addAction(R.drawable.ic_play_arrow_24, "Play", getPendingIntent(this, ACTION_RESUME))
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

    // send action to activity
    private void sendActionToActivity(int action) {
        Intent intentSend = new Intent(SEND_TO_ACTIVITY);
        // put data, something
        Bundle bundle = new Bundle();
        bundle.putSerializable(OBJ_SONG, currentObjSong);
        bundle.putBoolean(STATUS_PLAYING, isPlaying);
        bundle.putInt(KEY_SEND_ACTION, action);
        intentSend.putExtras(bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    // init for songs of service
    private void getAllMedia() {
        new GetAllMusicThread(MyMusicOfflineService.this, this.songs).start();
    }

    // init media player
    private void initMediaPlayer() {
        media = new MediaPlayer();
        currentSong = -1;

        // setting some properties
        media.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        media.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // set the class as listener
        media.setOnPreparedListener(this);
        media.setOnCompletionListener(this);
        media.setOnErrorListener(this);
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
