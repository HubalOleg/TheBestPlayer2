package com.oleg.hubal.thebestplayer.utility;

/**
 * Created by User on 28.11.2016.
 */

public interface OnPlayerActionListener {
    void changeCurrentSeekBarPosition(long currentPosition);
    void play();
    void pause();
    void next();
    void previous();
    void stop();
    void changeTrack();
    void queue(int position);
}
