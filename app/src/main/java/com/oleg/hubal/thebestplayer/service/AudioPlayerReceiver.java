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
        String action = intent.getStringExtra(ServiceConstants.PARAM_ACTION);

        switch (action) {
            case ServiceConstants.ACTION_PLAY:
                mOnPlayerActionListener.onPlay();
                break;
            case ServiceConstants.ACTION_PAUSE:
                mOnPlayerActionListener.onPause();
                break;
            case ServiceConstants.ACTION_NEXT:
                mOnPlayerActionListener.onNextTrack();
                break;
            case ServiceConstants.ACTION_PREVIOUS:
                mOnPlayerActionListener.onPreviousTrack();
                break;
            case ServiceConstants.ACTION_STOP:
                mOnPlayerActionListener.onStopMedia();
                break;
            case ServiceConstants.ACTION_CHANGE_TRACK:
                mOnPlayerActionListener.onChangeTrack();
                break;
            case ServiceConstants.ACTION_CHANGE_CURRENT_POSITION:
                int currentPosition = intent.getIntExtra(ServiceConstants.PARAM_CURRENT_POSITION, 0);
                mOnPlayerActionListener.onChangeTrackPosition(currentPosition);
                break;
            case ServiceConstants.ACTION_QUEUE:
                int queuePosition = intent.getIntExtra(ServiceConstants.PARAM_QUEUE_POSITION, 0);
                mOnPlayerActionListener.onTrackFromQueue(queuePosition);
                break;
        }
    }
}