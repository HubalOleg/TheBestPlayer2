package com.oleg.hubal.thebestplayer.utility;

/**
 * Created by User on 28.11.2016.
 */

public interface OnPlayerActionListener {
    void onChangeTrackPosition(long currentPosition);
    void onTrackFromQueue(int position);
    void onPreviousTrack();
    void onChangeTrack();
    void onNextTrack();
    void onStopMedia();
    void onPause();
    void onPlay();
}
