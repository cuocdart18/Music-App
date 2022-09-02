package com.example.musicapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicapp.fragments.FavouriteFragment;
import com.example.musicapp.fragments.PlaylistFragment;

public class ViewPager2ListSongAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 2;
    private PlaylistFragment playlistFragment = new PlaylistFragment();
    private FavouriteFragment favouriteFragment = new FavouriteFragment();

    public ViewPager2ListSongAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return playlistFragment;
            case 1:
                return favouriteFragment;
            default:
                return playlistFragment;
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }

    public PlaylistFragment getPlaylistFragment() {
        return this.playlistFragment;
    }

    public FavouriteFragment getFavouriteFragment() {
        return this.favouriteFragment;
    }
}
