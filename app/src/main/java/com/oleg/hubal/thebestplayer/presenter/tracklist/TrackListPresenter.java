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
import com.oleg.hubal.thebestplayer.service.ServiceConstants;
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

    public static final String SEARCH_ARTIST_SELECTION = MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?";
    public static final String SEARCH_TITLE_SELECTION = MediaStore.Audio.AudioColumns.TITLE + " LIKE ?";

    public static final String SORT_BY_DURATION = "Duration";
    public static final String SORT_BY_ARTIST = "Artist";
    public static final String SORT_BY_TITLE = "Title";
    public static final String SORT_NONE = "No sort";

    public static final String SEARCH_NONE = "No search";
    public static final String SEARCH_BY_ARTIST = "Artist";
    public static final String SEARCH_BY_TITLE = "Title";

    private TrackListViewContract mView;
    private final Context mContext;

    private AudioPlayerReceiver mPlayerReceiver;
    private MusicService mMusicService;
    private int mCurrentPosition = -1;
    private Intent mIntent;

    private List<Integer> mQueueList = new ArrayList();
    private List<TrackItem> mTrackItems;

    private String mCurrentSortOrder = SORT_NONE;

    private String[] mCursorLoaderSelectionArgs = null;
    private String mCursorLoaderSelection = null;

    private boolean isServiceBound = false;

//    CALLBACK

    private OnPlayerActionListener mOnPlayerActionListener = new OnPlayerActionListener() {
        @Override
        public void onPlay() {}

        @Override
        public void onPause() {}

        @Override
        public void onNextTrack() {
            mCurrentPosition++;
            if (mCurrentPosition >= mTrackItems.size())
                mCurrentPosition = 0;

            setSelectedItemAndScrollToPosition(mCurrentPosition);
        }

        @Override
        public void onPreviousTrack() {
            mCurrentPosition--;
            if (mCurrentPosition < 0)
                mCurrentPosition = mTrackItems.size() - 1;

            setSelectedItemAndScrollToPosition(mCurrentPosition);
        }

        @Override
        public void onStopMedia() {}

        @Override
        public void onChangeTrack() {}

        @Override
        public void onChangeTrackPosition(long currentPosition ) {}

        @Override
        public void onTrackFromQueue(int position) {
            mCurrentPosition = position;
            mView.showItemQueue(mCurrentPosition);

            for (int i : mQueueList)
                mView.showItemQueue(i);

            setSelectedItemAndScrollToPosition(mCurrentPosition);
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
                pullDataFromService();
            } else if (mTrackItems != null) {
                pushDataToService();
            }

            if (mMusicService.isQueueListExist()) {
                mQueueList = mMusicService.getQueueList();
            } else {
                pushQueueListToService();
            }

            if (mMusicService.getCurrentPosition() == -1 && mCurrentPosition != -1) {
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
        bindServiceIfExist();
    }

    @Override
    public void onTrackSelected(int position) {
        mCurrentPosition = position;
        mView.showSelectedItem(mCurrentPosition);

        if (Utils.isServiceRunning(MusicService.class.getName(), mContext)) {
            changeTrack();
        } else {
            launchService();
        }
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
    public void onQueueSelected(int itemPosition) {
        if (mTrackItems.get(itemPosition).getQueuePosition() == -1) {
            addToQueue(itemPosition);
        } else {
            removeFromQueue(itemPosition);
        }
        pushQueueListToService();
    }

    private void addToQueue(int itemPosition) {
        mQueueList.add(mQueueList.size(), itemPosition);
        mTrackItems.get(itemPosition).setQueuePosition(mQueueList.size());
        mView.showItemQueue(itemPosition);
    }

    private void removeFromQueue(int itemPosition) {
        int queuePosition = mTrackItems.get(itemPosition).getQueuePosition();
        for (int i = queuePosition; i < mQueueList.size(); i++) {
            TrackItem itemFromQueue = mTrackItems.get(mQueueList.get(i));
            int newPosition = itemFromQueue.getQueuePosition() - 1;
            itemFromQueue.setQueuePosition(newPosition);
            mView.showItemQueue(mQueueList.get(i));
        }
        mQueueList.remove(queuePosition - 1);
        mTrackItems.get(itemPosition).setQueuePosition(-1);
        mView.showItemQueue(itemPosition);
    }

    private void pushQueueListToService() {
        if (isServiceBound) {
            mMusicService.setQueueList(mQueueList);
        }
    }

    private void pullDataFromService() {
        mTrackItems = mMusicService.getTrackItems();
        mView.showTrackList(mTrackItems);
        mCurrentPosition = mMusicService.getCurrentPosition();
        if (mCurrentPosition != -1) {
            mView.scrollListToPosition(mCurrentPosition);
            mView.showSelectedItem(mCurrentPosition);
        }
    }

    private void pushDataToService() {
        mMusicService.setTrackItems(mTrackItems);
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
                !sortBy.equals(SORT_NONE) &&
                !sortBy.equals(mCurrentSortOrder)) {

            mQueueList.clear();
            for (TrackItem item : mTrackItems) {
                item.setQueuePosition(-1);
            }
            mView.unSelectAll();

            if (isServiceBound)
                mMusicService.onSearchSortAction();


            switch (sortBy) {
                case SORT_BY_DURATION:
                    Collections.sort(mTrackItems, new TrackDurationComparator());
                    break;
                case SORT_BY_ARTIST:
                    Collections.sort(mTrackItems, new TrackArtistComparator());
                    break;
                case SORT_BY_TITLE:
                    Collections.sort(mTrackItems, new TrackTitleComparator());
                    break;
            }
            mCurrentSortOrder = sortBy;
            mView.showSortedList();
        }
    }

    @Override
    public void onSearchItems(String searchBy, String searchKey) {
        mQueueList.clear();

        if (isServiceBound)
            mMusicService.onSearchSortAction();

        switch (searchBy) {
            case SEARCH_NONE:
                mCursorLoaderSelection = null;
                mCursorLoaderSelectionArgs = null;
                break;
            case SEARCH_BY_ARTIST:
                mCursorLoaderSelection = SEARCH_ARTIST_SELECTION;
                mCursorLoaderSelectionArgs = new String[] { "%" +  searchKey + "%"};
                break;
            case SEARCH_BY_TITLE:
                mCursorLoaderSelection = SEARCH_TITLE_SELECTION;
                mCursorLoaderSelectionArgs = new String[] { "%" +  searchKey + "%"};
                break;
        }
        mView.launchLoaderForSearch();
    }

    @Override
    public LoaderManager.LoaderCallbacks<Cursor> getTrackListLoader() {
        return mCursorLoader;
    }

    private CursorLoader createCursorLoader() {
        return new CursorLoader(mContext,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                mCursorLoaderSelection,
                mCursorLoaderSelectionArgs,
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

    private void setSelectedItemAndScrollToPosition(int position) {
        mView.scrollListToPosition(position);
        mView.showSelectedItem(position);
    }

    @Override
    public void onResume() {
        bindServiceIfExist();
        IntentFilter filter = new IntentFilter(ServiceConstants.BROADCAST_ACTION);
        mContext.registerReceiver(mPlayerReceiver, filter);
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mPlayerReceiver);
        if (isServiceBound) {
            mContext.unbindService(mMusicServiceConnection);
            isServiceBound = false;
        }
    }

    private void bindServiceIfExist() {
        if (Utils.isServiceRunning(MusicService.class.getName(), mContext) && !isServiceBound) {
            mContext.bindService(mIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
