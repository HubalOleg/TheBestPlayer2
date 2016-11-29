package com.oleg.hubal.thebestplayer.presenter.audioplayer;

import com.oleg.hubal.thebestplayer.presenter.BasePresenter;

/**
 * Created by User on 23.11.2016.
 */

public interface AudioPlayerPresenterContract extends BasePresenter {
    void onPlayPauseTrack();
    void onNextTrack();
    void onPreviousTrack();

    void onPause();
    void onResume();
    void onSeekTrackTo(int position);
}
