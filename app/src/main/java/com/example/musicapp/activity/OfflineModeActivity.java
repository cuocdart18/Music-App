package com.example.musicapp.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.adapter.ICallbackPlaylistFragment;
import com.example.musicapp.adapter.ViewPager2ListSongAdapter;
import com.example.musicapp.databinding.ActivityOfflineModeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OfflineModeActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityOfflineModeBinding binding;
    private ICallbackPlaylistFragment callbackPlaylistFragment = null;
    ViewPager2ListSongAdapter adapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init Template
        initTabViewpager2();

        // set on click for button music controller
        initOnClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initOnClickListener() {
        callbackPlaylistFragment = adapter.getPlaylistFragment();
        binding.layoutMusicController.imvShuffle.setOnClickListener(this);
        binding.layoutMusicController.imvSkipPrev.setOnClickListener(this);
        binding.layoutMusicController.imvPlayPause.setOnClickListener(this);
        binding.layoutMusicController.imvSkipNext.setOnClickListener(this);
        binding.layoutMusicController.imvLoop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (binding.layoutMusicController.imvShuffle.getId() == id) {
            this.callbackPlaylistFragment.onClickShuffle();
        } else if (binding.layoutMusicController.imvSkipPrev.getId() == id) {
            this.callbackPlaylistFragment.onClickSkipPrev();
        } else if (binding.layoutMusicController.imvPlayPause.getId() == id) {
            this.callbackPlaylistFragment.onClickPlayPause();
        } else if (binding.layoutMusicController.imvSkipNext.getId() == id) {
            this.callbackPlaylistFragment.onClickSkipNext();
        } else if (binding.layoutMusicController.imvLoop.getId() == id) {
            this.callbackPlaylistFragment.onClickLoop();
        }
    }

    private void initTabViewpager2() {
        adapter = new ViewPager2ListSongAdapter(this);
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

    public ActivityOfflineModeBinding getBinding() {
        return binding;
    }
}
