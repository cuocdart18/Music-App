package com.example.musicapp.manager;

import static com.example.musicapp.AppUtils.SAVE_DATA_LOCAL;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {
    private Context context;

    public MySharedPreferences(Context context) {
        this.context = context;
    }

    public void putBooleanValue(String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_DATA_LOCAL,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_DATA_LOCAL,
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, true);
    }
}
