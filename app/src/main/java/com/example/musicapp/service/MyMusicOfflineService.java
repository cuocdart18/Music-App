package com.example.musicapp.service;

import static com.example.musicapp.AppUtils.ACTION_INIT_UI;
import static com.example.musicapp.AppUtils.ACTION_LOOP;
import static com.example.musicapp.AppUtils.ACTION_NEXT;
import static com.example.musicapp.AppUtils.ACTION_PAUSE;
import static com.example.musicapp.AppUtils.ACTION_PREV;
import static com.example.musicapp.AppUtils.ACTION_RESUME;
import static com.example.musicapp.AppUtils.ACTION_SHUFFLE;
import static com.example.musicapp.AppUtils.ACTION_SORT;
import static com.example.musicapp.AppUtils.ACTION_START;
import static com.example.musicapp.AppUtils.ACTION_STOP;
import static com.example.musicapp.AppUtils.ACTION_UPDATE_TIME;
import static com.example.musicapp.AppUtils.DATA_OPTION_SORT;
import static com.example.musicapp.AppUtils.FINAL_TIME;
import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;
import static com.example.musicapp.AppUtils.KEY_SEND_ACTION;
import static com.example.musicapp.AppUtils.OBJ_SONG;
import static com.example.musicapp.AppUtils.POSITION;
import static com.example.musicapp.AppUtils.PROGRESS;
import static com.example.musicapp.AppUtils.SEND_LIST_SHUFFLE_SORT_SONG;
import static com.example.musicapp.AppUtils.SEND_LIST_SONG;
import static com.example.musicapp.AppUtils.SEND_SONGS_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.SEND_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.SEND_UPDATE_TIME_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.SORT_A_Z;
import static com.example.musicapp.AppUtils.SORT_DEFAULT;
import static com.example.musicapp.AppUtils.SORT_Z_A;
import static com.example.musicapp.AppUtils.START_TIME;
import static com.example.musicapp.AppUtils.STATUS_LOOPING;
import static com.example.musicapp.AppUtils.STATUS_PLAYING;
import static com.example.musicapp.AppUtils.STATUS_SHUFFLE;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class MyMusicOfflineService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private long currentSong = -1;  // id of song
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

    // handler update time
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (media != null) {
                startTime = media.getCurrentPosition();
                updateCurrentTime();
            }
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
        // init media
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // receive songs by context
        if (this.songs == null) {
            this.songs = (List<Song>) intent.getSerializableExtra(SEND_LIST_SONG);
        }

        handleActionFromBroadcast(intent);
        return START_NOT_STICKY;
    }

    private void handleActionFromBroadcast(Intent intent) {
        action = intent.getIntExtra(KEY_RECEIVE_ACTION, 0);

        switch (action) {
            case ACTION_PAUSE:
                handleActionPause(intent);
                break;
            case ACTION_RESUME:
                handleActionResume(intent);
                break;
            case ACTION_NEXT:
                handleActionNext(intent);
                break;
            case ACTION_PREV:
                handleActionPrev(intent);
                break;
            case ACTION_STOP:
                handleActionStop(intent);
                break;
            case ACTION_START:
                handleActionStart(intent);
                break;
            case ACTION_SHUFFLE:
                handleActionShuffle(intent);
                break;
            case ACTION_LOOP:
                handleActionLoop(intent);
                break;
            case ACTION_UPDATE_TIME:
                handleActionUpdateTime(intent);
                break;
            case ACTION_INIT_UI:
                handleActionInitUi(intent);
            case ACTION_SORT:
                handleActionSort(intent);
                break;
        }
    }

    private void handleActionSort(Intent intent) {
        Bundle bundle = intent.getExtras();
        int option = bundle.getInt(DATA_OPTION_SORT);

        if (option == SORT_DEFAULT) {
            songs = AppUtils.getInstance(this).getAllMediaMp3Files();
        } else {
            sortSongsByOption(songs, option);
        }
        sendSongsToActivity(songs);
        // auto play the first song in list
        currentObjSong = songs.get(0);
        positionSong = 0;
        action = ACTION_START;
        playMusic(currentObjSong);
    }

    private void handleActionShuffle(Intent intent) {
        // turn off/on shuffle
        if (isShuffling) {
            songs = AppUtils.getInstance(this).getAllMediaMp3Files();
            // update list songs to show for user
            sendSongsToActivity(songs);
            isShuffling = false;
        } else {
            shuffleSongs(songs);
            // update list songs to show for user
            sendSongsToActivity(songs);
            isShuffling = true;
        }
        // auto play the first song in list
        currentObjSong = songs.get(0);
        positionSong = 0;
        action = ACTION_START;
        playMusic(currentObjSong);
    }

    private void handleActionInitUi(Intent intent) {
        if (positionSong == -1)
            return;

        if (songs != null) {
            sendSongsToActivity(songs);
            updateCurrentTime();
        }

        sendActionToActivity(action);
    }

    private void handleActionStart(Intent intent) {
        Bundle bundle = intent.getExtras();
        positionSong = bundle.getInt(POSITION, 0);
        currentObjSong = songs.get(positionSong);

        playMusic(currentObjSong);
    }

    private void handleActionResume(Intent intent) {
        if (positionSong == -1)
            return;
        //
        resumeMusic();
    }

    private void handleActionPause(Intent intent) {
        if (positionSong == -1)
            return;
        //
        pauseMusic();
    }

    private void handleActionNext(Intent intent) {
        if (positionSong == -1)
            return;
        //
        nextMusic();
    }

    private void handleActionPrev(Intent intent) {
        if (positionSong == -1)
            return;
        //
        prevMusic();
    }

    private void handleActionLoop(Intent intent) {
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

    private void handleActionUpdateTime(Intent intent) {
        if (media != null) {
            Bundle bundle = intent.getExtras();
            media.seekTo(bundle.getInt(PROGRESS));
            startTime = media.getCurrentPosition();
            updateCurrentTime();
        }
    }

    private void handleActionStop(Intent intent) {
        stopSelf();
        sendActionToActivity(action);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        // get time
        startTime = mp.getCurrentPosition();
        finalTime = mp.getDuration();

        // update current UI
        updateCurrentTime();

        isPlaying = true;

        // update notification
        sendNotificationMediaStyle();
        // send response action to activity
        sendActionToActivity(action);
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
            updateCurrentTime();
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
        Intent intentContext = new Intent(MyMusicOfflineService.this, OfflineModeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyMusicOfflineService.this,
                0, intentContext, PendingIntent.FLAG_UPDATE_CURRENT);
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
        bundle.putBoolean(STATUS_LOOPING, isLooping);
        bundle.putBoolean(STATUS_SHUFFLE, isShuffling);
        bundle.putDouble(FINAL_TIME, finalTime);
        bundle.putInt(KEY_SEND_ACTION, action);
        intentSend.putExtras(bundle);
        // send
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    // send a list song to activity
    private void sendSongsToActivity(List<Song> sendSongs) {
        Intent intentSend = new Intent(SEND_SONGS_TO_ACTIVITY);
        // put data, something
        intentSend.putExtra(SEND_LIST_SHUFFLE_SORT_SONG, (Serializable) sendSongs);
        // send
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    // send time of song to activity
    private void sendTimeOfSongToActivity() {
        Intent intentSend = new Intent(SEND_UPDATE_TIME_TO_ACTIVITY);
        // put data, something
        Bundle bundle = new Bundle();
        bundle.putDouble(START_TIME, startTime);
        intentSend.putExtras(bundle);
        // send
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSend);
    }

    // update current time to activity
    private void updateCurrentTime() {
        sendTimeOfSongToActivity();
        handler.postDelayed(runnable, 1000);
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
        isPlaying = false;
        songs = null;
        currentObjSong = null;
        positionSong = -1;
        currentSong = -1;
    }

    private void shuffleSongs(List<Song> songsOrigin) {
        Random random = new Random();
        for (int i = 0; i < songsOrigin.size(); i++) {
            int randomIndexToSwap = random.nextInt(songsOrigin.size());
            swapTwoObjSongs(songsOrigin, i, randomIndexToSwap);
        }
    }

    private void sortSongsByOption(List<Song> songsOrigin, int option) {
        char c1, c2;
        for (int i = 0; i < songsOrigin.size() - 1; i++) {
            for (int j = i + 1; j < songsOrigin.size(); j++) {
                c1 = songsOrigin.get(i).getTitle().charAt(0);
                c2 = songsOrigin.get(j).getTitle().charAt(0);
                if (option == SORT_A_Z && c1 > c2) {
                    swapTwoObjSongs(songsOrigin, i, j);
                } else if (option == SORT_Z_A && c1 < c2) {
                    swapTwoObjSongs(songsOrigin, i, j);
                }
            }
        }
    }

    private void swapTwoObjSongs(List<Song> songsSwap, int i, int j) {
        Song songTmp = songsSwap.get(i);

        songsSwap.set(i, songsSwap.get(j));
        songsSwap.get(i).setPosInList(i);

        songsSwap.set(j, songTmp);
        songsSwap.get(j).setPosInList(j);
    }
}
