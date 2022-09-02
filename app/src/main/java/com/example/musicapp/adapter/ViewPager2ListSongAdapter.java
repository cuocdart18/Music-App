package com.example.musicapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicapp.fragments.FavouriteFragment;
import com.example.musicapp.fragments.PlaylistFragment;

public class ViewPager2ListSongAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 2;

    public ViewPager2ListSongAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlaylistFragment();
            case 1:
                return new FavouriteFragment();
            default:
                return new PlaylistFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
