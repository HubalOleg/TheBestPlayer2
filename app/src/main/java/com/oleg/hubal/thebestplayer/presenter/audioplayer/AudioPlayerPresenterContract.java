package com.oleg.hubal.thebestplayer.presenter.audioplayer;

/**
 * Created by User on 23.11.2016.
 */

public interface AudioPlayerPresenterContract {
    void onPlayPauseTrack();
    void onNextTrack();
    void onPreviousTrack();

    void onPause();
    void onResume();
    void onSeekTrackTo(int position);
    void onLoopTrack();
}
