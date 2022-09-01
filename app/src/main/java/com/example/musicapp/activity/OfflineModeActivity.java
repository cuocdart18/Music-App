package com.example.musicapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.databinding.ActivityOfflineModeBinding;
import com.example.musicapp.models.Song;

import java.util.List;


public class OfflineModeActivity extends AppCompatActivity {
    private static final String KEY_PASS_LIST_SONG = "key_pass_list_song";
    private ActivityOfflineModeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intentReceiver = getIntent();
        List<Song> songs = (List<Song>) intentReceiver.getSerializableExtra(KEY_PASS_LIST_SONG);


    }
}
