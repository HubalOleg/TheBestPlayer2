package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;

import com.oleg.hubal.thebestplayer.presenter.BasePresenter;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListPresenterContract extends BasePresenter {

    LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader();
    void onFillTrackList();
    void onTrackSelected(int position);
    void onQueueSelected(int itemPosition);
    void onPause();
    void onResume();

}
