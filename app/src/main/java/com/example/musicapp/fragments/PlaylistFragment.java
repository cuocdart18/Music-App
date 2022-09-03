package com.example.musicapp.fragments;

import static com.example.musicapp.AppUtils.ACTION_LOOP;
import static com.example.musicapp.AppUtils.ACTION_NEXT;
import static com.example.musicapp.AppUtils.ACTION_PAUSE;
import static com.example.musicapp.AppUtils.ACTION_PREV;
import static com.example.musicapp.AppUtils.ACTION_RESUME;
import static com.example.musicapp.AppUtils.ACTION_SEEK;
import static com.example.musicapp.AppUtils.ACTION_SHUFFLE;
import static com.example.musicapp.AppUtils.ACTION_START;
import static com.example.musicapp.AppUtils.ACTION_STOP;
import static com.example.musicapp.AppUtils.DEFAULT_TITLE;
import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;
import static com.example.musicapp.AppUtils.KEY_SEND_ACTION;
import static com.example.musicapp.AppUtils.OBJ_SONG;
import static com.example.musicapp.AppUtils.POSITION;
import static com.example.musicapp.AppUtils.PROGRESS;
import static com.example.musicapp.AppUtils.SEND_LIST_SONG;
import static com.example.musicapp.AppUtils.SEND_TO_ACTIVITY;
import static com.example.musicapp.AppUtils.STATUS_PLAYING;

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
import com.example.musicapp.adapter.ICallbackPlaylistFragment;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.databinding.ActivityOfflineModeBinding;
import com.example.musicapp.databinding.FragmentPlaylistOfflineModeBinding;
import com.example.musicapp.models.Song;
import com.example.musicapp.service.MyMusicOfflineService;

import java.io.Serializable;
import java.util.List;

public class PlaylistFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, ICallbackOnClickItem {
    private FragmentPlaylistOfflineModeBinding binding;
    private OfflineModeActivity offlineModeActivity;
    private ActivityOfflineModeBinding bindingActivity;

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
    public double startTime = 0;
    public double finalTime = 0;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistOfflineModeBinding.inflate(inflater, container, false);

        offlineModeActivity = (OfflineModeActivity) getActivity();
        bindingActivity = offlineModeActivity.getBinding();

        // regis broadcast
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,
                new IntentFilter(SEND_TO_ACTIVITY));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // init template
        initConfigSwipeRefreshLayout();
        initDataInRecycler(); // call data song from device

        // call service and send list song
        Intent intentCallService = new Intent(getContext(), MyMusicOfflineService.class);
        intentCallService.putExtra(SEND_LIST_SONG, (Serializable) songs);
        getContext().startService(intentCallService);

        // init data simple
        titleCurrentMusic.set(DEFAULT_TITLE);
        startTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        // set on click seekbar
        onClickSeekBarChangeListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    // this method handle any actions from broadcast
    private void handleActionFromService(Bundle bundle) {
        int action = bundle.getInt(KEY_SEND_ACTION);

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
                handleActionStopFromService();
                break;
            case ACTION_START:
                handleActionStartFromService(bundle);
                break;
            case ACTION_SHUFFLE:
                break;
            case ACTION_LOOP:
                break;
            case ACTION_SEEK:
                break;
        }
    }

    private void handleActionStartFromService(Bundle bundle) {
        currentObjSong = (Song) bundle.get(OBJ_SONG);
        // set UI
        isPlaying.set(bundle.getBoolean(STATUS_PLAYING));
        titleCurrentMusic.set(currentObjSong.getTitle());
    }

    private void handleActionStopFromService() {
        currentObjSong = null;
        // set UI
        isPlaying.set(false);
        isShuffling.set(false);
        isLooping.set(false);
        titleCurrentMusic.set(DEFAULT_TITLE);
        startTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
        finalTimeText.set(AppUtils.getInstance(getContext()).formatTime(0));
    }

    @Override
    public void onClickItemInRecycler(Song song) {
        Toast.makeText(getContext(), "play music " + song.getPosInList(), Toast.LENGTH_SHORT).show();
        sendPositionToMusicService(ACTION_START, song.getPosInList());
    }

    @Override
    public void onClickImvFavourite(Song song) {
        Toast.makeText(getContext(), "fav", Toast.LENGTH_SHORT).show();
    }

    public void onClickShuffle() {
        Toast.makeText(getContext(), "shuffle", Toast.LENGTH_SHORT).show();
        sendActionToMusicService(ACTION_SHUFFLE);
    }

    public void onClickSkipPrev() {
        Toast.makeText(getContext(), "prev", Toast.LENGTH_SHORT).show();
        sendActionToMusicService(ACTION_PREV);
    }

    public void onClickPlayPause() {
        Toast.makeText(getContext(), "play pause", Toast.LENGTH_SHORT).show();
        if (Boolean.TRUE.equals(isPlaying.get())) {
            sendActionToMusicService(ACTION_PAUSE);
        } else {
            sendActionToMusicService(ACTION_RESUME);
        }
    }

    public void onClickSkipNext() {
        Toast.makeText(getContext(), "next", Toast.LENGTH_SHORT).show();
        sendActionToMusicService(ACTION_NEXT);
    }

    public void onClickLoop() {
        Toast.makeText(getContext(), "loop", Toast.LENGTH_SHORT).show();
        sendActionToMusicService(ACTION_LOOP);
    }

    private void onClickSeekBarChangeListener() {
        bindingActivity.layoutMusicController.sbMusicTimeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    // send action to service by start (go to startCommand)
    private void sendActionToMusicService(int action) {
        Intent intentToService = new Intent(getContext(), MyMusicOfflineService.class);
        intentToService.putExtra(KEY_RECEIVE_ACTION, action);
        getContext().startService(intentToService);
    }

    // update progress by start (go to startCommand)
    private void sendActionToMusicService(int action, int progress) {
        Intent intentToService = new Intent(getContext(), MyMusicOfflineService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(PROGRESS, progress);
        intentToService.putExtras(bundle);
        intentToService.putExtra(KEY_RECEIVE_ACTION, action);
        getContext().startService(intentToService);
    }

    // send position of song to service by start (go to startCommand)
    private void sendPositionToMusicService(int action, int position) {
        Intent intentToService = new Intent(getContext(), MyMusicOfflineService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);
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
        songs = AppUtils.getInstance(getContext()).getAllMediaMp3Files();
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
