package com.example.musicapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.databinding.FragmentPlaylistOfflineModeBinding;

public class PlaylistFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentPlaylistOfflineModeBinding binding;
    private ListSongRecyclerAdapter adapter = new ListSongRecyclerAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistOfflineModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initConfigSwipeRefreshLayout();
        initDataInRecycler();
    }

    private void initDataInRecycler() {
        binding.rclvPlaylistSong.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rclvPlaylistSong.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        binding.rclvPlaylistSong.addItemDecoration(decoration);

        adapter.setDataChange(AppUtils.getInstance(getContext()).getAllMediaMp3Files());
    }

    private void initConfigSwipeRefreshLayout() {
        binding.refreshListMusicLayout.setOnRefreshListener(this::onRefresh);
        binding.refreshListMusicLayout.setColorSchemeColors(getResources().getColor(R.color.teal_700));
    }

    @Override
    public void onRefresh() {
        adapter.setDataChange(AppUtils.getInstance(getContext()).getAllMediaMp3Files());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.refreshListMusicLayout.setRefreshing(false);
            }
        }, 1000);
    }
}
