package com.oleg.hubal.thebestplayer.view.tracklist;

import com.oleg.hubal.thebestplayer.model.TrackItem;

import java.util.List;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListViewContract {

    void showTrackList(List<TrackItem> trackList);
    void showSelectedItem(int position);
    void showItemQueue(int itemPosition);
    void showSortedList();
    void unSelectAll();
    void scrollListToPosition(int position);
    void launchTrackListLoader();
    void launchLoaderForSearch();
}
