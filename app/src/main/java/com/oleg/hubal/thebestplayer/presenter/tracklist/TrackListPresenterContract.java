package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.presenter.BasePresenter;

/**
 * Created by User on 22.11.2016.
 */

public interface TrackListPresenterContract extends BasePresenter {

    LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader();
    TrackItem getTrackItemByPosition(int position);
    int getCurrentPosition();
    void onFillTrackList();
    void onTrackSelected(int position);
    void onPause();
    void onResume();

}
