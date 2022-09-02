package com.example.musicapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.adapter.ICallbackOnClickItem;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.databinding.FragmentPlaylistOfflineModeBinding;
import com.example.musicapp.models.Song;
import com.example.musicapp.service.MyMusicOfflineService;

import java.util.List;

public class PlaylistFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ICallbackOnClickItem {
    private FragmentPlaylistOfflineModeBinding binding;
    private ListSongRecyclerAdapter adapter = new ListSongRecyclerAdapter(this);
    private List<Song> songs;

    // receive event from service
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

        // regis broadcast
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter(MyMusicOfflineService.SEND_TO_ACTIVITY));

        // call service
        Intent intentCallService = new Intent(getContext(), MyMusicOfflineService.class);
        getContext().startService(intentCallService);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // init template
        initConfigSwipeRefreshLayout();
        initDataInRecycler();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    // this method handle any actions from broadcast
    private void handleActionFromService(Bundle bundle) {
        int action = bundle.getInt(MyMusicOfflineService.KEY_SEND_ACTION);

        switch (action) {
            case 1:
                break;
        }
    }

    @Override
    public void onClickItemInRecycler(Song song) {
        Toast.makeText(getContext(), "play music", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickImvFavourite(Song song) {
        Toast.makeText(getContext(), "fav", Toast.LENGTH_SHORT).show();
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
