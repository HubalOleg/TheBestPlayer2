package com.oleg.hubal.thebestplayer.presenter.tracklist;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.service.AudioPlayerReceiver;
import com.oleg.hubal.thebestplayer.service.MusicService;
import com.oleg.hubal.thebestplayer.utility.Constants;
import com.oleg.hubal.thebestplayer.utility.OnPlayerActionListener;
import com.oleg.hubal.thebestplayer.utility.TrackArtistComparator;
import com.oleg.hubal.thebestplayer.utility.TrackDurationComparator;
import com.oleg.hubal.thebestplayer.utility.TrackTitleComparator;
import com.oleg.hubal.thebestplayer.utility.Utils;
import com.oleg.hubal.thebestplayer.view.tracklist.TrackListViewContract;

import java.util.ArrayList;
import java.util.Collections;
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

    private List<Integer> mQueueList = new ArrayList();

    private AudioPlayerReceiver mPlayerReceiver;

    private MusicService mMusicService;
    private int mCurrentPosition = -1;

    private String mCurrentSortOrder = Constants.SORT_NONE;

    private boolean isServiceBound = false;

//    CALLBACK

    private OnPlayerActionListener mOnPlayerActionListener = new OnPlayerActionListener() {
        @Override
        public void play() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void next() {
            mCurrentPosition++;
            if (mCurrentPosition >= mTrackItems.size()) {
                mCurrentPosition = 0;
            }

            mView.setSelectedItem(mCurrentPosition);
            mView.scrollListToPosition(mCurrentPosition);
        }

        @Override
        public void previous() {
            mCurrentPosition--;
            if (mCurrentPosition < 0)
                mCurrentPosition = mTrackItems.size() - 1;

            mView.setSelectedItem(mCurrentPosition);
            mView.scrollListToPosition(mCurrentPosition);
        }

        @Override
        public void stop() {

        }

        @Override
        public void changeTrack() {

        }

        @Override
        public void changeCurrentPosition(long currentPosition ) {
        }

        @Override
        public void queue(int position) {
            mCurrentPosition = position;
            mView.setItemQueue(mCurrentPosition);

            for (int i : mQueueList) {
                mView.setItemQueue(i);
            }

            mView.setSelectedItem(mCurrentPosition);
            mView.scrollListToPosition(mCurrentPosition);
        }
    };

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

    private ServiceConnection mMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            mMusicService = binder.getService();
            isServiceBound = true;

            if (mMusicService.isTrackListExist()) {
                mTrackItems = mMusicService.getTrackItems();
                mView.setTrackItems(mTrackItems);
                mCurrentPosition = mMusicService.getCurrentPosition();
            } else if (mTrackItems != null) {
                mMusicService.setTrackItems(mTrackItems);
            }

            if (mMusicService.isQueueListExist()) {
                mQueueList = mMusicService.getQueueList();
            } else {
                setQueueListToService();
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
        mPlayerReceiver = new AudioPlayerReceiver(mOnPlayerActionListener);
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

    @Override
    public void onQueueSelected(int itemPosition) {
        if (mTrackItems.get(itemPosition).getQueuePosition() == -1) {
            addToQueue(itemPosition);
        } else {
            removeFromQueue(itemPosition);
        }
        setQueueListToService();
    }

    private void setQueueListToService() {
        if (isServiceBound) {
            mMusicService.setQueueList(mQueueList);
        }
    }

    private void addToQueue(int itemPosition) {
        mQueueList.add(mQueueList.size(), itemPosition);
        mTrackItems.get(itemPosition).setQueuePosition(mQueueList.size());
        mView.setItemQueue(itemPosition);
    }

    private void removeFromQueue(int itemPosition) {
        int queuePosition = mTrackItems.get(itemPosition).getQueuePosition();
        for (int i = queuePosition; i < mQueueList.size(); i++) {
            TrackItem itemFromQueue = mTrackItems.get(mQueueList.get(i));
            int newPosition = itemFromQueue.getQueuePosition() - 1;
            itemFromQueue.setQueuePosition(newPosition);
            mView.setItemQueue(mQueueList.get(i));
        }
        mQueueList.remove(queuePosition - 1);
        mTrackItems.get(itemPosition).setQueuePosition(-1);
        mView.setItemQueue(itemPosition);
    }

    private void changeTrack() {
        if (mMusicService != null) {
            mMusicService.playTrackByPosition(mCurrentPosition);
        }
    }

    private void launchService() {
        mContext.startService(mIntent);
        mContext.bindService(mIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader() {
        return mCursorLoader;
    }

    @Override
    public void onFillTrackList() {
        if (!Utils.isServiceRunning(MusicService.class.getName(), mContext)) {
            mView.launchTrackListLoader();
        }
    }

    @Override
    public void onSortItems(String sortBy) {
        if (mTrackItems != null &&
                !sortBy.equals(Constants.SORT_NONE) &&
                !sortBy.equals(mCurrentSortOrder)) {

            mQueueList.clear();
            for (TrackItem item : mTrackItems) {
                item.setQueuePosition(-1);
            }
            mView.unSelectAll();

            if (isServiceBound)
                mMusicService.stopMedia();

            switch (sortBy) {
                case Constants.SORT_BY_DURATION:
                    Collections.sort(mTrackItems, new TrackDurationComparator());
                    break;
                case Constants.SORT_BY_ARTIST:
                    Collections.sort(mTrackItems, new TrackArtistComparator());
                    break;
                case Constants.SORT_BY_TITLE:
                    Collections.sort(mTrackItems, new TrackTitleComparator());
                    break;
            }
            mCurrentSortOrder = sortBy;
            mView.showSortedList();
        }
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
            mContext.bindService(mIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mPlayerReceiver);
        if (isServiceBound) {
            mContext.unbindService(mMusicServiceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        bindServiceIfRunning();
        IntentFilter filter = new IntentFilter(AudioPlayerReceiver.BROADCAST_ACTION);
        mContext.registerReceiver(mPlayerReceiver, filter);
    }

    @Override
    public void onStop() {
    }
}
