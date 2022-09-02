package com.example.musicapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.musicapp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    public static final String SEND_TO_ACTIVITY = "send_data_to_activity";

    public static final String KEY_SEND_ACTION = "send_action";
    public static final String KEY_RECEIVE_ACTION = "receive_action";

    public static final int ACTION_PAUSE = 1001;
    public static final int ACTION_RESUME = 1002;
    public static final int ACTION_NEXT = 1003;
    public static final int ACTION_PREV = 1004;
    public static final int ACTION_STOP = 1005;
    public static final int ACTION_START = 1006;
    public static final int ACTION_SHUFFLE = 1007;
    public static final int ACTION_LOOP = 1008;

    private static AppUtils instance;
    private Context context;

    private AppUtils(Context context) {
        this.context = context;
    }

    public static AppUtils getInstance(Context context) {
        if (instance == null) {
            instance = new AppUtils(context);
        }
        return instance;
    }

    public boolean isNetworkAvailable() {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    public List<Song> getAllMediaMp3Files() {
        List<Song> songs = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor == null) {
            Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_SHORT);
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(context, "No Music Found on SD Card.", Toast.LENGTH_SHORT);
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
