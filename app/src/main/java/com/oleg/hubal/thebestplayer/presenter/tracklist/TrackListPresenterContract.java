package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListPresenterContract {

    LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader();
    void onFillTrackList();
    void onTrackSelected(int position);
    void onQueueSelected(int itemPosition);
    void onPause();
    void onResume();
    void onSortItems(String sortBy);
    void onSearchItems(String searchBy, String searchKey);
}
