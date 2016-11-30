package com.oleg.hubal.thebestplayer.utility;

/**
 * Created by User on 28.11.2016.
 */

public interface OnPlayerActionListener {
    void onChangeTrackPosition(long currentPosition);
    void onPlay();
    void onPause();
    void onNextTrack();
    void onPreviousTrack();
    void onStopMedia();
    void onChangeTrack();
    void onTrackFromQueue(int position);
}
