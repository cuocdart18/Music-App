package com.example.musicapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnMusicOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "online mode", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMusicOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBtnOfflineMode();
            }
        });
    }

    private void onClickBtnOfflineMode() {

    }

}
