package com.example.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.databinding.FragmentPlaylistOfflineModeBinding;

public class PlaylistFragment extends Fragment {
    private FragmentPlaylistOfflineModeBinding binding;
    private ListSongRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistOfflineModeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDataInRecycler();
    }

    private void initDataInRecycler() {
        adapter = new ListSongRecyclerAdapter();

        binding.rclvPlaylistSong.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rclvPlaylistSong.setAdapter(adapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        binding.rclvPlaylistSong.addItemDecoration(decoration);
        adapter.setDataChange(AppUtils.getInstance(getContext()).getAllMediaMp3Files());
    }
}
