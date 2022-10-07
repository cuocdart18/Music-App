package com.example.musicapp.broadcast_receiver;

import static com.example.musicapp.AppUtils.ACTION_RESUME;
import static com.example.musicapp.AppUtils.ACTION_STOP;
import static com.example.musicapp.AppUtils.KEY_RECEIVE_ACTION;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.musicapp.service.MyMusicOfflineService;

public class StateEarphoneBroadcast extends BroadcastReceiver {
    private static final String TAG = StateEarphoneBroadcast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent intentSendToService = new Intent(context, MyMusicOfflineService.class);

        // with bluetooth devices
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            intentSendToService.putExtra(KEY_RECEIVE_ACTION, ACTION_RESUME);
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            intentSendToService.putExtra(KEY_RECEIVE_ACTION, ACTION_STOP);
        }

        // with jack headphone device
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0: // unplugged
                    intentSendToService.putExtra(KEY_RECEIVE_ACTION, ACTION_STOP);
                    break;
                case 1: // plugged
                    intentSendToService.putExtra(KEY_RECEIVE_ACTION, ACTION_RESUME);
                    break;
                default:
            }
        }

        // send
        context.startService(intentSendToService);
    }
}
