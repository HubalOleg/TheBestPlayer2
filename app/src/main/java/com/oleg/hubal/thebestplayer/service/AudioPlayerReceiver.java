package com.oleg.hubal.thebestplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oleg.hubal.thebestplayer.utility.OnPlayerActionListener;

/**
 * Created by User on 28.11.2016.
 */

public class AudioPlayerReceiver extends BroadcastReceiver {

   OnPlayerActionListener mOnPlayerActionListener;

    public AudioPlayerReceiver(OnPlayerActionListener onPlayerActionListener) {
        mOnPlayerActionListener = onPlayerActionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(MusicService.PARAM_ACTION);

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
        }
    }
}