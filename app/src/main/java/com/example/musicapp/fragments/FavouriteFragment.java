package com.example.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.musicapp.databinding.FragmentFavouriteOfflineModeBinding;

public class FavouriteFragment extends Fragment {
    private FragmentFavouriteOfflineModeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavouriteOfflineModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
