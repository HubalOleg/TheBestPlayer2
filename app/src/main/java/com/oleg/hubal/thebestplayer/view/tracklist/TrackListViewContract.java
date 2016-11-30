package com.oleg.hubal.thebestplayer.view.tracklist;

import com.oleg.hubal.thebestplayer.model.TrackItem;

import java.util.List;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListViewContract {

    void setSelectedItem(int position);
    void showTrackItems(List<TrackItem> trackItems);
    void launchTrackListLoader();
    void setItemQueue(int itemPosition);
    void scrollListToPosition(int position);
    void showSortedList();
    void unSelectAll();
    void launchLoaderForSearch();
}
