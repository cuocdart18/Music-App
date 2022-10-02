package com.example.musicapp.activity;

import static com.example.musicapp.AppUtils.ACTION_SHUFFLE;
import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;
import static com.example.musicapp.AppUtils.SORT_A_Z;
import static com.example.musicapp.AppUtils.SORT_DEFAULT;
import static com.example.musicapp.AppUtils.SORT_Z_A;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.adapter.ViewPager2ListSongAdapter;
import com.example.musicapp.databinding.ActivityOfflineModeBinding;
import com.example.musicapp.fragments.PlaylistFragment;
import com.example.musicapp.service.MyMusicOfflineService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OfflineModeActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityOfflineModeBinding binding;
    private ViewPager2ListSongAdapter adapter = null;
    private PlaylistFragment playlistFragment = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init Template
        initTabViewpager2();
        // init data binding
        binding.layoutMusicController.setPlaylistFragment(adapter.getPlaylistFragment());

        // set click for 3 image view
        binding.topBarOfflineMode.imvShuffle.setOnClickListener(this);
        binding.topBarOfflineMode.imvFilter.setOnClickListener(this);
        binding.topBarOfflineMode.imvSearch.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == binding.topBarOfflineMode.imvShuffle.getId()) {
            onClickBtnShuffleInTopBar();
        } else if (id == binding.topBarOfflineMode.imvFilter.getId()) {
            onClickBtnFilterInTopBar();
        } else if (id == binding.topBarOfflineMode.imvSearch.getId()) {
            onClickBtnSearchInTopBar();
        }
    }

    private void onClickBtnShuffleInTopBar() {
        if (playlistFragment == null)
            return;
        playlistFragment.onClickShuffleBtnInTopBar();
    }

    private void onClickBtnSearchInTopBar() {
    }

    private void onClickBtnFilterInTopBar() {
        if (playlistFragment == null)
            return;

        // show dialog for user choose sort mode
        String[] items = {"Default", "A-Z", "Z-A"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by ?");
        builder.setCancelable(true);
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("A-Z")) {
                    playlistFragment.onClickSortBtnInTopBar(SORT_A_Z);
                } else if (items[which].equals("Z-A")) {
                    playlistFragment.onClickSortBtnInTopBar(SORT_Z_A);
                } else if (items[which].equals("Default")) {
                    playlistFragment.onClickSortBtnInTopBar(SORT_DEFAULT);
                }
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setPlaylistFragment(PlaylistFragment playlistFragment) {
        this.playlistFragment = playlistFragment;
    }

}
