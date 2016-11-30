package com.oleg.hubal.thebestplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oleg.hubal.thebestplayer.utility.OnPlayerActionListener;

/**
 * Created by User on 28.11.2016.
 */

public class AudioPlayerReceiver extends BroadcastReceiver {

    public static final String BROADCAST_ACTION = "com.oleg.hubal.thebestplayer.ACTION_BROADCAST";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_CURRENT_POSITION = "current_position";
    public static final String PARAM_QUEUE_POSITION = "queue_position";

   OnPlayerActionListener mOnPlayerActionListener;

    public AudioPlayerReceiver(OnPlayerActionListener onPlayerActionListener) {
        mOnPlayerActionListener = onPlayerActionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(PARAM_ACTION);

        switch (action) {
            case MusicService.ACTION_PLAY:
                mOnPlayerActionListener.onPlay();
                break;
            case MusicService.ACTION_PAUSE:
                mOnPlayerActionListener.onPause();
                break;
            case MusicService.ACTION_NEXT:
                mOnPlayerActionListener.onNextTrack();
                break;
            case MusicService.ACTION_PREVIOUS:
                mOnPlayerActionListener.onPreviousTrack();
                break;
            case MusicService.ACTION_STOP:
                mOnPlayerActionListener.onStopMedia();
                break;
            case MusicService.ACTION_CHANGE_TRACK:
                mOnPlayerActionListener.onChangeTrack();
                break;
            case MusicService.ACTION_CHANGE_CURRENT_POSITION:
                mOnPlayerActionListener.onChangeTrackPosition(intent.getIntExtra(PARAM_CURRENT_POSITION, 0));
                break;
            case MusicService.ACTION_QUEUE:
                mOnPlayerActionListener.onTrackFromQueue(intent.getIntExtra(PARAM_QUEUE_POSITION, 0));
                break;
        }
    }
}