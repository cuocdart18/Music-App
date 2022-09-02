package com.example.musicapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musicapp.AppUtils;
import com.example.musicapp.R;
import com.example.musicapp.adapter.ListSongRecyclerAdapter;
import com.example.musicapp.adapter.ViewPager2ListSongAdapter;
import com.example.musicapp.databinding.ActivityOfflineModeBinding;
import com.example.musicapp.models.Song;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;


public class OfflineModeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private ActivityOfflineModeBinding binding;
    private List<Song> songs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init Template
        initConfigSwipeRefreshLayout();
        initTabViewpager2();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        this.songs = AppUtils.getInstance(this).getAllMediaMp3Files();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.listSong.refreshListMusicLayout.setRefreshing(false);
            }
        }, 1500);
    }

    private void initConfigSwipeRefreshLayout() {
        binding.listSong.refreshListMusicLayout.setOnRefreshListener(this::onRefresh);
        binding.listSong.refreshListMusicLayout.setColorSchemeColors(getResources().getColor(R.color.teal_700));
    }

    private void initTabViewpager2() {
        ViewPager2ListSongAdapter adapter = new ViewPager2ListSongAdapter(this);
        binding.listSong.vpg2ListMusic.setAdapter(adapter);

        new TabLayoutMediator(binding.listSong.tabPlaylistFavour,
                binding.listSong.vpg2ListMusic,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Playlist");
                                break;
                            case 1:
                                tab.setText("My favourite");
                                break;
                            default:
                                break;
                        }
                    }
                }).attach();
    }
}
