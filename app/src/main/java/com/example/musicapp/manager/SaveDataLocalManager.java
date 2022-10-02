package com.example.musicapp.manager;

import static com.example.musicapp.AppUtils.KEY_IS_FIRST_INSTALL;

import android.content.Context;

public class SaveDataLocalManager {
    private static SaveDataLocalManager instance;
    private MySharedPreferences mySharedPreferences;

    public static void init(Context context) {
        instance = new SaveDataLocalManager();
        instance.mySharedPreferences = new MySharedPreferences(context);
    }

    public static SaveDataLocalManager getInstance() {
        if (instance == null) {
            instance = new SaveDataLocalManager();
        }
        return instance;
    }

    public void setFirstInstallApp(boolean isFirst) {
        SaveDataLocalManager.getInstance().mySharedPreferences.putBooleanValue(KEY_IS_FIRST_INSTALL, isFirst);
    }

    public boolean getFirstInstallApp() {
        return SaveDataLocalManager.getInstance().mySharedPreferences.getBooleanValue(KEY_IS_FIRST_INSTALL);
    }
}
