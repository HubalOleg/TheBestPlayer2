package com.oleg.hubal.thebestplayer.utility;

import com.oleg.hubal.thebestplayer.model.TrackItem;

import java.util.Comparator;

/**
 * Created by User on 29.11.2016.
 */

public class TrackArtistComparator implements Comparator<TrackItem> {
    @Override
    public int compare(TrackItem trackItem, TrackItem trackItemNext) {
        return trackItem.getArtist().compareTo(trackItemNext.getArtist());
    }
}
