package com.oleg.hubal.thebestplayer.presenter.audioplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.service.AudioPlayerReceiver;
import com.oleg.hubal.thebestplayer.service.MusicService;
import com.oleg.hubal.thebestplayer.utility.OnPlayerActionListener;
import com.oleg.hubal.thebestplayer.utility.Utils;
import com.oleg.hubal.thebestplayer.view.audioplayer.AudioPlayerViewContract;

/**
 * Created by User on 23.11.2016.
 */

public class AudioPlayerPresenter implements AudioPlayerPresenterContract {

    private static final String TAG = "AudioPlayerPresenter";

    private final Context mContext;
    private AudioPlayerViewContract mView;

    private MusicService mMusicService;
    private boolean isServiceBound = false;
    private Intent mIntent;

    private AudioPlayerReceiver mPlayerReceiver;

    private TrackItem mCurrentItem;

//    CALLBACK AND LISTENERS

    private OnPlayerActionListener mOnPlayerActionListener = new OnPlayerActionListener() {
        @Override
        public void play() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void next() {

        }

        @Override
        public void previous() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void changeTrack() {
            if (isServiceBound) {
                mCurrentItem = mMusicService.getCurrentItem();
                mView.showTrackInfo(mCurrentItem);
            } else {
                bindServiceIfRunning();
            }
        }
    };

    private ServiceConnection mMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            mMusicService = binder.getService();

            isServiceBound = true;

            if (mMusicService.isTrackListExist()) {
                mCurrentItem = mMusicService.getCurrentItem();
                mView.showTrackInfo(mCurrentItem);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

//    CALLBACK AND LISTENERS END

    public AudioPlayerPresenter(Context context, AudioPlayerViewContract view) {
        mContext = context;
        mView = view;
        mPlayerReceiver = new AudioPlayerReceiver(mOnPlayerActionListener);
        mIntent = new Intent(mContext, MusicService.class);
        bindServiceIfRunning();
    }

    @Override
    public void onPlayPauseAction() {
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {
        if (isServiceBound) {
            mContext.unbindService(mMusicServiceConnection);
            mContext.unregisterReceiver(mPlayerReceiver);
            isServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        bindServiceIfRunning();
        IntentFilter filter = new IntentFilter(AudioPlayerReceiver.BROADCAST_ACTION);
        mContext.registerReceiver(mPlayerReceiver, filter);
    }

    private void bindServiceIfRunning() {
        if (Utils.isServiceRunning(MusicService.class.getName(), mContext) && !isServiceBound) {
            mContext.bindService(mIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
