package com.oleg.hubal.thebestplayer.presenter.audioplayer;

import android.content.Context;

import com.oleg.hubal.thebestplayer.utility.PlayerConstants;
import com.oleg.hubal.thebestplayer.view.audioplayer.AudioPlayerViewContract;

/**
 * Created by User on 23.11.2016.
 */

public class AudioPlayerPresenter implements AudioPlayerPresenterContract {

    private final Context mContext;
    private AudioPlayerViewContract mView;

    public AudioPlayerPresenter(Context context, AudioPlayerViewContract view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void onPlayPauseAction() {
        PlayerConstants.PLAY_PAUSE_HANDLER.sendEmptyMessage(0);
    }

    @Override
    public void onStop() {

    }
}
