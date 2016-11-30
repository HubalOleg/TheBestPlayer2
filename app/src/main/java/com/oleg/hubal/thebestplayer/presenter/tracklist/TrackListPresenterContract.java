package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListPresenterContract {

    LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader();
    void onSearchItems(String searchBy, String searchKey);
    void onQueueSelected(int itemPosition);
    void onTrackSelected(int position);
    void onSortItems(String sortBy);
    void onFillTrackList();
    void onResume();
    void onPause();
}
