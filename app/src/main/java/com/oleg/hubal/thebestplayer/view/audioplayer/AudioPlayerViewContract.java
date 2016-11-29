package com.oleg.hubal.thebestplayer.view.audioplayer;

import com.oleg.hubal.thebestplayer.model.TrackItem;

/**
 * Created by User on 23.11.2016.
 */

public interface AudioPlayerViewContract {
    void showTrackInfo(TrackItem trackItem);
    void onUpdatePlayPauseButton(boolean isPlaying);
    void changeSeekBarPosition(int position);
    void changeTrackPositionTextView(String currentPosition);
}
