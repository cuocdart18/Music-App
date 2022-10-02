package com.example.musicapp.fragments;

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
import static com.example.musicapp.AppUtils.DEFAULT_TITLE;
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
import static com.example.musicapp.AppUtils.START_TIME;
import static com.example.musicapp.AppUtils.STATUS_LOOPING;
import static com.example.musicapp.AppUtils.STATUS_PLAYING;
import static com.example.musicapp.AppUtils.STATUS_SHUFFLE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.activity.OfflineModeActivity;
import com.example.musicapp.adapter.ICallbackOnClickItem;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.databinding.FragmentPlaylistOfflineModeBinding;
import com.example.musicapp.models.Song;
import com.example.musicapp.service.MyMusicOfflineService;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PlaylistFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, ICallbackOnClickItem {
    private FragmentPlaylistOfflineModeBinding binding;
    private OfflineModeActivity offlineModeActivity;

    private ListSongRecyclerAdapter adapter = new ListSongRecyclerAdapter(this);
    private List<Song> songs;
    private Song currentObjSong;

    // live data
    public ObservableField<String> titleCurrentMusic = new ObservableField<>();
    public ObservableField<Boolean> isPlaying = new ObservableField<>();
    public ObservableField<Boolean> isShuffling = new ObservableField<>();
    public ObservableField<Boolean> isLooping = new ObservableField<>();
    public ObservableField<String> startTimeText = new ObservableField<>();
    public ObservableField<String> finalTimeText = new ObservableField<>();
    public ObservableField<Integer> progressPlay = new ObservableField<>();
    public ObservableField<Integer> progressMax = new ObservableField<>();
    public ObservableField<Boolean> isServiceDestroyed = new ObservableField<>();

    // receive from service
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            if (bundle == null) {
                return;
            }

            //handle action
            handleActionFromService(bundle);
        }
    };

    private BroadcastReceiver receiverSongs = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Song> songsFromService = (List<Song>) intent.getSerializableExtra(SEND_LIST_SHUFFLE_SORT_SONG);
            if (songsFromService != null) {
                songs = songsFromService;
                // set a new adapter
                updateDataInRecycler(songs);
            }
        }
    };

    private BroadcastReceiver receiverUpdateTimeline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            startTimeText.set(AppUtils.getInstance(getContext()).formatTime(bundle.getDouble(START_TIME)));
            progressPlay.set((int) bundle.getDouble(START_TIME));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistOfflineModeBinding.inflate(inflater, container, false);

        // regis broadcast
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,
                new IntentFilter(SEND_TO_ACTIVITY));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiverSongs,
                new IntentFilter(SEND_SONGS_TO_ACTIVITY));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiverUpdateTimeline,
                new IntentFilter(SEND_UPDATE_TIME_TO_ACTIVITY));

        // set state for service
        isServiceDestroyed.set(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        offlineModeActivity = (OfflineModeActivity) getActivity();
        Objects.requireNonNull(offlineModeActivity).setPlaylistFragment(this);

        // load all songs from device
        songs = AppUtils.getInstance(getContext()).getAllMediaMp3Files();

        // init template
        initConfigSwipeRefreshLayout();
        initDataInRecycler(); // call data song from device

        // call service and send list song
        callService();

        // init data simple
        titleCurrentMusic.set(DEFAULT_TITLE);
        startTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));

        // request to service -> set ui if service is playing music
        sendActionToMusicService(ACTION_INIT_UI);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiverSongs);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiverUpdateTimeline);
    }

    // this method handle any actions from broadcast
    private void handleActionFromService(Bundle bundle) {
        int action = bundle.getInt(KEY_SEND_ACTION);

        switch (action) {
            case ACTION_PAUSE:
                handleActionPauseFromService(bundle);
                break;
            case ACTION_RESUME:
                handleActionResumeFromService(bundle);
                break;
            case ACTION_NEXT:
                handleActionNextFromService(bundle);
                break;
            case ACTION_PREV:
                handleActionPrevFromService(bundle);
                break;
            case ACTION_STOP:
                handleActionStopFromService();
                break;
            case ACTION_START:
                handleActionStartFromService(bundle);
                break;
            case ACTION_SHUFFLE:
                handleActionShuffleFromService(bundle);
                break;
            case ACTION_LOOP:
                handleActionLoopFromService(bundle);
                break;
            case ACTION_INIT_UI:
                handleActionInitUiFromService(bundle);
                break;
            case ACTION_UPDATE_TIME:
                handleActionUpdateStartTimeFromService(bundle);
                break;
        }
    }

    private void handleActionInitUiFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        isLooping.set(bundle.getBoolean(STATUS_LOOPING));
        isShuffling.set(bundle.getBoolean(STATUS_SHUFFLE));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(bundle.getDouble(FINAL_TIME)));
        progressMax.set((int) bundle.getDouble(FINAL_TIME));
        titleCurrentMusic.set(currentObjSong.getTitle());
    }

    private void handleActionStartFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        isShuffling.set(bundle.getBoolean(STATUS_SHUFFLE));
        isLooping.set(bundle.getBoolean(STATUS_LOOPING));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(bundle.getDouble(FINAL_TIME)));
        progressMax.set((int) bundle.getDouble(FINAL_TIME));
        titleCurrentMusic.set(currentObjSong.getTitle());
    }

    private void handleActionResumeFromService(Bundle bundle) {
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
    }

    private void handleActionPauseFromService(Bundle bundle) {
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
    }

    private void handleActionNextFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(bundle.getDouble(FINAL_TIME)));
        progressMax.set((int) bundle.getDouble(FINAL_TIME));
        titleCurrentMusic.set(currentObjSong.getTitle());
    }

    private void handleActionPrevFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(bundle.getDouble(FINAL_TIME)));
        progressMax.set((int) bundle.getDouble(FINAL_TIME));
        titleCurrentMusic.set(currentObjSong.getTitle());
    }

    private void handleActionLoopFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        isLooping.set(bundle.getBoolean(STATUS_LOOPING));
    }

    private void handleActionShuffleFromService(Bundle bundle) {
    }

    private void handleActionUpdateStartTimeFromService(Bundle bundle) {
    }

    private void handleActionStopFromService() {
        currentObjSong = null;
        // set UI
        isPlaying.set(false);
        isShuffling.set(false);
        isLooping.set(false);
        isServiceDestroyed.set(true);
        titleCurrentMusic.set(DEFAULT_TITLE);
        startTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        progressPlay.set(0);
        progressMax.set(0);
    }

    @Override
    public void onClickItemInRecycler(Song song) {
        // if service destroyed, call a new service
        callService();
        sendDataToMusicService(ACTION_START, song.getPosInList(), POSITION);
    }

    @Override
    public void onClickImvFavourite(Song song) {
        Toast.makeText(getContext(), "fav", Toast.LENGTH_SHORT).show();
    }

    public void onClickShuffle() {
        sendActionToMusicService(ACTION_SHUFFLE);
    }

    public void onClickSkipPrev() {
        sendActionToMusicService(ACTION_PREV);
    }

    public void onClickPlayPause() {
        if (Boolean.TRUE.equals(isPlaying.get())) {
            sendActionToMusicService(ACTION_PAUSE);
        } else {
            sendActionToMusicService(ACTION_RESUME);
        }
    }

    public void onClickSkipNext() {
        sendActionToMusicService(ACTION_NEXT);
    }

    public void onClickLoop() {
        sendActionToMusicService(ACTION_LOOP);
    }

    public void onClickShuffleBtnInTopBar() {
        callService();
        sendActionToMusicService(ACTION_SHUFFLE);
    }

    public void onClickSortBtnInTopBar(int option) {
        // send action sort to service
        callService();
        sendDataToMusicService(ACTION_SORT, option, DATA_OPTION_SORT);
    }

    // TODO: some bugs about: click at progress bar then auto play the first song
    public void onUserStopTrackingTouchSeekbar(SeekBar seekBar) {
        if (Boolean.TRUE.equals(isServiceDestroyed.get()) && Boolean.FALSE.equals(isPlaying.get())) {
            return;
        }
        progressPlay.set(seekBar.getProgress());
        sendDataToMusicService(ACTION_UPDATE_TIME, progressPlay.get(), PROGRESS);
    }

    // send action to service by start (go to startCommand)
    private void sendActionToMusicService(int action) {
        Intent intentToService = new Intent(getContext(), MyMusicOfflineService.class);
        intentToService.putExtra(KEY_RECEIVE_ACTION, action);
        getContext().startService(intentToService);
    }

    // update data by start (go to startCommand)
    public void sendDataToMusicService(int action, int data, String KEY) {
        Intent intentToService = new Intent(getContext(), MyMusicOfflineService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY, data);
        intentToService.putExtras(bundle);
        intentToService.putExtra(KEY_RECEIVE_ACTION, action);
        getContext().startService(intentToService);
    }

    // fill data (mp3 file) for recycler view
    private void initDataInRecycler() {
        binding.rclvPlaylistSong.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rclvPlaylistSong.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        binding.rclvPlaylistSong.addItemDecoration(decoration);
        updateDataInRecycler(songs);
    }

    // update data in recycler with other songs
    private void updateDataInRecycler(List<Song> songs) {
        if (adapter != null) {
            adapter.setDataChange(songs);
        }
    }

    // setting something in swipe refresh layout
    private void initConfigSwipeRefreshLayout() {
        binding.refreshListMusicLayout.setOnRefreshListener(this::onRefresh);
        binding.refreshListMusicLayout.setColorSchemeColors(getResources().getColor(R.color.teal_700));
    }

    // call service if destroyed
    private void callService() {
        if (Boolean.TRUE.equals(isServiceDestroyed.get())) {
            Intent intentCallService = new Intent(getContext(), MyMusicOfflineService.class);
            intentCallService.putExtra(SEND_LIST_SONG, (Serializable) songs);
            getContext().startService(intentCallService);
            isServiceDestroyed.set(false);
        }
    }

    @Override
    public void onRefresh() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.refreshListMusicLayout.setRefreshing(false);
            }
        }, 1000);
    }

}
