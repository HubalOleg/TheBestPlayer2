package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.service.MusicService;
import com.oleg.hubal.thebestplayer.utility.Utils;
import com.oleg.hubal.thebestplayer.view.tracklist.TrackListViewContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 22.11.2016.
 */

public class TrackListPresenter implements TrackListPresenterContract {

    private static final String TAG = "TrackListPresenter";

    private final Context mContext;
    private Intent mIntent;
    private TrackListViewContract mView;
    private List<TrackItem> mTrackItems;

    private MusicService mMusicService;
    private int mCurrentPosition = 0;

    private boolean isServiceBound = false;

//    CALLBACK

    private LoaderManager.LoaderCallbacks<Cursor> mCursorLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return createCursorLoader();
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mTrackItems = parseCursorData(data);
            mView.showTrackList(mTrackItems);

            if (isServiceBound) {
                mMusicService.setTrackItems(mTrackItems);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private ServiceConnection mMusicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            mMusicService = binder.getService();

            if (mMusicService.isTrackListExist()) {
                Log.d(TAG, "onServiceConnected: " + mMusicService.isTrackListExist());
                mView.setTrackItems(mMusicService.getTrackItems());
            }

            isServiceBound = true;

            if (mTrackItems != null) {
                mMusicService.setTrackItems(mTrackItems);
            }

            if (!mMusicService.isPlaying()) {
                mMusicService.playTrackByPosition(mCurrentPosition);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

//    CALLBACK END

    public TrackListPresenter(Context context, TrackListViewContract view) {
        mContext = context;
        mView = view;
        mIntent = new Intent(mContext, MusicService.class);
        bindServiceIfRunning();
    }

    @Override
    public void onTrackSelected(int position) {
        mCurrentPosition = position;
        mView.setSelectedItem(mCurrentPosition);

        if (Utils.isServiceRunning(MusicService.class.getName(), mContext)) {
            changeTrack();
        } else {
            launchService();
        }
    }

    private void changeTrack() {
        mMusicService.playTrackByPosition(mCurrentPosition);
        mView.setSelectedItem(mCurrentPosition);
    }

    private void launchService() {
        mContext.startService(mIntent);
        mContext.bindService(mIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader() {
        return mCursorLoader;
    }

    @Override
    public TrackItem getTrackItemByPosition(int position) {
        return mTrackItems.get(position);
    }

    @Override
    public void onFillTrackList() {
        if (!Utils.isServiceRunning(MusicService.class.getName(), mContext)) {
            mView.launchTrackListLoader();
        }
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    private CursorLoader createCursorLoader() {
        return new CursorLoader(mContext,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    private List<TrackItem> parseCursorData(Cursor data) {
        List<TrackItem> trackItems = new ArrayList<>();

        if (data.moveToFirst()) {
            do {
                String path = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
                int albumId = data.getInt(data.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ID));
                Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                String albumImage = uri.toString();
                String artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                artist = artist.replace("<unknown>", "");
                String title = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
                long duration = data.getLong(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
                trackItems.add(new TrackItem(path, albumImage, artist, title, duration));
            } while (data.moveToNext());
        }

        return trackItems;
    }

    private void bindServiceIfRunning() {
        if (Utils.isServiceRunning(MusicService.class.getName(), mContext) && !isServiceBound) {
            mContext.bindService(mIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        if (isServiceBound) {
            mContext.unbindService(mMusicConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        bindServiceIfRunning();
    }

    @Override
    public void onStop() {
    }
}
