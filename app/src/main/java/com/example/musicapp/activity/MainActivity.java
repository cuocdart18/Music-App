package com.example.musicapp.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicapp.AppUtils;
import com.example.musicapp.databinding.ActivityMainBinding;
import com.example.musicapp.models.Song;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_READ_EXTERNAL = 1001;
    private long backPressedTime = 0;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnMusicOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBtnOnlineMode();
            }
        });

        binding.btnMusicOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBtnOfflineMode();
            }
        });
    }

    private void onClickBtnOnlineMode() {
        if (!AppUtils.getInstance(this).isNetworkAvailable()) {
            Toast.makeText(this, "Network is disconnected", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, OnlineModeActivity.class);
            startActivity(intent);
        }
    }

    private void onClickBtnOfflineMode() {
        // if permission granted, get all file
        multipleRequestPermission();
    }

    // check permission
    private void multipleRequestPermission() {
        //check API > 23, la tu apk 6.0 tro len
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Intent intent = new Intent(MainActivity.this, OfflineModeActivity.class);
            startActivity(intent);
            return;
        }

        // check xem Permission da duoc cap phep (GRANTED) hay chua
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MainActivity.this, OfflineModeActivity.class);
            startActivity(intent);
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, REQUEST_PERMISSION_READ_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // check request code = voi REQUEST_FINE_LOCATION
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL) {
            // neu nhu dong y
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, OfflineModeActivity.class);
                startActivity(intent);
            } else {
                // neu nhu tu choi
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(this, "Pressed back again to exit the application", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
