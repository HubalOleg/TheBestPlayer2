package com.oleg.hubal.thebestplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.oleg.hubal.thebestplayer.utility.OnPlayerActionListener;

/**
 * Created by User on 28.11.2016.
 */

public class AudioPlayerReceiver extends BroadcastReceiver {

    private static final String TAG = "AudioPlayerReceiver";

    public static final String BROADCAST_ACTION = "com.oleg.hubal.thebestplayer.ACTION_BROADCAST";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_CURRENT_POSITION = "current_position";

   OnPlayerActionListener mOnPlayerActionListener;

    public AudioPlayerReceiver(OnPlayerActionListener onPlayerActionListener) {
        mOnPlayerActionListener = onPlayerActionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(PARAM_ACTION);

        switch (action) {
            case MusicService.ACTION_PLAY:
                mOnPlayerActionListener.play();
                break;
            case MusicService.ACTION_PAUSE:
                mOnPlayerActionListener.pause();
                break;
            case MusicService.ACTION_NEXT:
                mOnPlayerActionListener.next();
                break;
            case MusicService.ACTION_PREVIOUS:
                mOnPlayerActionListener.previous();
                break;
            case MusicService.ACTION_STOP:
                mOnPlayerActionListener.stop();
                break;
            case MusicService.ACTION_CHANGE_TRACK:
                mOnPlayerActionListener.changeTrack();
                Log.d(TAG, "onReceive: ");
                break;
            case MusicService.ACTION_CHANGE_CURRENT_POSITION:
                mOnPlayerActionListener.changeCurrentPosition(intent.getIntExtra(PARAM_CURRENT_POSITION, 0));
                break;
        }
    }
}