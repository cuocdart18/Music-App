package com.example.musicapp.broadcast_receiver;

import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;
import static com.example.musicapp.AppUtils.KEY_SEND_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.musicapp.service.MyMusicOfflineService;

public class PlayMusicOfflineBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(KEY_SEND_ACTION, 0);

        Intent intentSendToService = new Intent(context, MyMusicOfflineService.class);
        intentSendToService.putExtra(KEY_RECEIVE_ACTION, action);

        // send to service
        context.startService(intentSendToService);
    }
}
