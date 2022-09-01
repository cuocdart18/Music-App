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
    private static final String KEY_PASS_LIST_SONG = "key_pass_list_song";
    private ActivityMainBinding binding;
    private List<Song> songs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if permission granted, get all file
        multipleRequestPermission();

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
        if (!AppUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Network is disconnected", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, OnlineModeActivity.class);
            startActivity(intent);
        }
    }

    private void onClickBtnOfflineMode() {
        Intent intent = new Intent(MainActivity.this, OfflineModeActivity.class);
        intent.putExtra(KEY_PASS_LIST_SONG, (Serializable) songs);
        startActivity(intent);
    }

    // check permission
    private void multipleRequestPermission() {
        //check API > 23, la tu apk 6.0 tro len
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.songs = getAllMediaMp3Files();
            return;
        }

        // check xem Permission da duoc cap phep (GRANTED) hay chua
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            this.songs = getAllMediaMp3Files();
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
                this.songs = getAllMediaMp3Files();
            } else {
                // neu nhu tu choi
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Song> getAllMediaMp3Files() {
        List<Song> songs = new ArrayList<>();

        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor == null) {
            Toast.makeText(this, "Something Went Wrong.", Toast.LENGTH_SHORT);
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(this, "No Music Found on SD Card.", Toast.LENGTH_SHORT);
        } else {
            //get columns
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String thisArtist = cursor.getString(artistColumn);
                songs.add(new Song(thisId, thisTitle, thisArtist));
            } while (cursor.moveToNext());
        }

        return songs;
    }
}
