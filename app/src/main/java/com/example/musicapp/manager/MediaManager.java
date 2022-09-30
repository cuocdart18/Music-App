package com.example.musicapp.manager;

import static com.example.musicapp.AppUtils.ACTION_NEXT;
import static com.example.musicapp.AppUtils.ACTION_PAUSE;
import static com.example.musicapp.AppUtils.ACTION_PREV;
import static com.example.musicapp.AppUtils.ACTION_RESUME;
import static com.example.musicapp.AppUtils.ACTION_STOP;
import static com.example.musicapp.AppUtils.ACTION_UPDATE_TIME;
import static com.example.musicapp.AppUtils.FINAL_TIME;
import static com.example.musicapp.AppUtils.KEY_SEND_ACTION;
import static com.example.musicapp.AppUtils.OBJ_SONG;
import static com.example.musicapp.AppUtils.POSITION;
import static com.example.musicapp.AppUtils.PROGRESS;
import static com.example.musicapp.AppUtils.SEND_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.START_TIME;
import static com.example.musicapp.AppUtils.STATUS_LOOPING;
import static com.example.musicapp.AppUtils.STATUS_PLAYING;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.musicapp.R;
import com.example.musicapp.activity.OfflineModeActivity;
import com.example.musicapp.application.MyApplication;
import com.example.musicapp.broadcast_receiver.PlayMusicOfflineBroadcast;
import com.example.musicapp.models.Song;
import com.example.musicapp.service.MyMusicOfflineService;

import java.io.IOException;
import java.util.List;

public class MediaManager implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private long currentSong = -1;
    private int positionSong = -1;
    private int action = 0;

    private double startTime = 0;
    private double finalTime = 0;

    private boolean isPlaying;
    private boolean isLooping;
    private boolean isShuffling;

    private Song currentObjSong;
    private List<Song> songs;
    private MediaPlayer media;
    private NotificationCompat.Builder notiBuilder;

    //    private Context context;
    private MyMusicOfflineService myMusicOfflineService;

    // handle update current position song
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (media != null) {
                startTime = media.getCurrentPosition();
                updateCurrentTime(ACTION_UPDATE_TIME);
            }
        }
    };

    public MediaManager(MyMusicOfflineService myMusicOfflineService) {
        this.myMusicOfflineService = myMusicOfflineService;
        initMediaPlayer();
    }

    public void handleActionInitUi(Intent intent) {
        if (positionSong == -1)
            return;
        sendActionToActivity(action);
    }

    public void handleActionStart(Intent intent) {
        Bundle bundle = intent.getExtras();
        positionSong = bundle.getInt(POSITION, 0);
        currentObjSong = songs.get(positionSong);

        playMusic(currentObjSong);
    }

    public void handleActionResume(Intent intent) {
        if (positionSong == -1)
            return;
        //
        resumeMusic();
    }

    public void handleActionPause(Intent intent) {
        if (positionSong == -1)
            return;
        //
        pauseMusic();
    }

    public void handleActionNext(Intent intent) {
        if (positionSong == -1)
            return;
        //
        nextMusic();
    }

    public void handleActionPrev(Intent intent) {
        if (positionSong == -1)
            return;
        //
        prevMusic();
    }

    public void handleActionLoop(Intent intent) {
        if (positionSong == -1)
            return;
        //
        if (isLooping) {
            isLooping = false;
        } else {
            isLooping = true;
        }
        sendActionToActivity(action);
    }

    public void handleActionUpdateTime(Intent intent) {
        if (media != null) {
            Bundle bundle = intent.getExtras();
            media.seekTo(bundle.getInt(PROGRESS));
            updateCurrentTime(ACTION_UPDATE_TIME);
        }
    }

    // stop service before call this method
    public void handleActionStop(Intent intent) {
        sendActionToActivity(action);
    }

    private void playMusic(Song currentObjSong) {
        media.reset();
        //get id
        currentSong = currentObjSong.getId();
        //set Uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        //setting this uri as data source
        try {
            media.setDataSource(this.myMusicOfflineService.getApplicationContext(), trackUri);
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        //complete the play song
        media.prepareAsync();
    }

    private void pauseMusic() {
        if (isPlaying && media != null) {
            media.pause();
            isPlaying = false;
            sendNotificationMediaStyle();
            sendActionToActivity(action);
        }
    }

    private void resumeMusic() {
        if (media != null && !isPlaying) {
            media.start();
            isPlaying = true;
            startTime = media.getCurrentPosition();
            updateCurrentTime(ACTION_UPDATE_TIME);
            sendNotificationMediaStyle();
            sendActionToActivity(action);
        }
    }

    private void nextMusic() {
        if (positionSong == songs.size() - 1) {
            positionSong = 0;
        } else {
            positionSong++;
        }
        currentObjSong = songs.get(positionSong);
        playMusic(currentObjSong);
    }

    private void prevMusic() {
        if (positionSong == 0) {
            positionSong = songs.size() - 1;
        } else {
            positionSong--;
        }
        currentObjSong = songs.get(positionSong);
        playMusic(currentObjSong);
    }

    // send notification for start music
    private void sendNotificationMediaStyle() {
        Intent intentContext = new Intent(this.myMusicOfflineService, OfflineModeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.myMusicOfflineService,
                0, intentContext, PendingIntent.FLAG_UPDATE_CURRENT);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this.myMusicOfflineService, "tag");

        if (currentObjSong != null) {
            notiBuilder = new NotificationCompat.Builder(this.myMusicOfflineService, MyApplication.CHANNEL_ID)
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
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this.myMusicOfflineService, ACTION_PREV))
                    .addAction(R.drawable.ic_pause_24, "Pause", getPendingIntent(this.myMusicOfflineService, ACTION_PAUSE))
                    .addAction(R.drawable.ic_skip_next_24, "Next", getPendingIntent(this.myMusicOfflineService, ACTION_NEXT))
                    .addAction(R.drawable.ic_stop_24, "Stop", getPendingIntent(this.myMusicOfflineService, ACTION_STOP));
        } else {
            notiBuilder
                    .clearActions()
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_skip_previous_24, "Previous", getPendingIntent(this.myMusicOfflineService, ACTION_PREV))
                    .addAction(R.drawable.ic_play_arrow_24, "Play", getPendingIntent(this.myMusicOfflineService, ACTION_RESUME))
                    .addAction(R.drawable.ic_skip_next_24, "Next", getPendingIntent(this.myMusicOfflineService, ACTION_NEXT))
                    .addAction(R.drawable.ic_stop_24, "Stop", getPendingIntent(this.myMusicOfflineService, ACTION_STOP));
        }

        notiBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2) // btn Previous, Next, Pause in Collapsed
                .setMediaSession(mediaSessionCompat.getSessionToken()));

        Notification notification = notiBuilder.build();

//        this.myMusicOfflineService.startForegroundWithMediaManager(1, notification);

    }

    // send data by pendingIntent to broadcast receiver from Notification (to service)
    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this.myMusicOfflineService, PlayMusicOfflineBroadcast.class);
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
        bundle.putBoolean(STATUS_LOOPING, isLooping);
        bundle.putDouble(START_TIME, startTime);
        bundle.putDouble(FINAL_TIME, finalTime);
        bundle.putInt(KEY_SEND_ACTION, action);
        intentSend.putExtras(bundle);
        // send data to update UI
        LocalBroadcastManager.getInstance(this.myMusicOfflineService).sendBroadcast(intentSend);
    }

    // update current time to activity
    private void updateCurrentTime(int action) {
        sendActionToActivity(action);
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (media == null) {
            return;
        }

        if (isLooping) {
            mp.start();
        } else {
            // auto next
            action = ACTION_NEXT;
            nextMusic();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        // get time
        startTime = mp.getCurrentPosition();
        finalTime = mp.getDuration();

        // update current UI
        updateCurrentTime(ACTION_UPDATE_TIME);

        isPlaying = true;

        // update notification
        sendNotificationMediaStyle();
        // send response action to activity
        sendActionToActivity(action);
    }

    // init media player
    private void initMediaPlayer() {
        media = new MediaPlayer();
        currentSong = -1;

        // setting some properties
        media.setWakeMode(this.myMusicOfflineService.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        media.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // set the class as listener
        media.setOnPreparedListener(this);
        media.setOnCompletionListener(this);
        media.setOnErrorListener(this);
    }

    public void destroyMediaManager() {
        if (media != null) {
            media.release();
            media = null;
        }
        isPlaying = false;
        songs = null;
        currentObjSong = null;
        positionSong = -1;
        currentSong = -1;
    }

    // GETTER or SETTER
    public long getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(long currentSong) {
        this.currentSong = currentSong;
    }

    public int getPositionSong() {
        return positionSong;
    }

    public void setPositionSong(int positionSong) {
        this.positionSong = positionSong;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
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

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    public boolean isShuffling() {
        return isShuffling;
    }

    public void setShuffling(boolean shuffling) {
        isShuffling = shuffling;
    }

    public Song getCurrentObjSong() {
        return currentObjSong;
    }

    public void setCurrentObjSong(Song currentObjSong) {
        this.currentObjSong = currentObjSong;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
