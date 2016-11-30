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

    private AudioPlayerViewContract mView;
    private final Context mContext;

    private AudioPlayerReceiver mPlayerReceiver;
    private boolean isServiceBound = false;
    private MusicService mMusicService;
    private Intent mIntent;

    private long mCurrentTrackDuration = 0;
    private long mCurrentTrackPosition;
    private TrackItem mCurrentItem;

    private boolean isPlaying = false;
    private boolean isLooping = false;

//    CALLBACK AND LISTENERS

    private OnPlayerActionListener mOnPlayerActionListener = new OnPlayerActionListener() {

        @Override
        public void onPlay() {
            mView.onUpdatePlayPauseButton(isPlaying = true);
        }

        @Override
        public void onPause() {
            mView.onUpdatePlayPauseButton(isPlaying = false);
        }

        @Override
        public void onNextTrack() {
            mView.onUpdatePlayPauseButton(isPlaying = true);
        }

        @Override
        public void onPreviousTrack() {
            mView.onUpdatePlayPauseButton(isPlaying = true);
        }

        @Override
        public void onStopMedia() {
            mView.clearTrackInfo();
            mView.onUpdatePlayPauseButton(isPlaying = false);
        }

        @Override
        public void onChangeTrack() {
            mView.onUpdatePlayPauseButton(isPlaying = true);
            if (isServiceBound) {
                mCurrentItem = mMusicService.getCurrentItem();
                mView.showTrackInfo(mCurrentItem);
            } else {
                bindServiceIfExist();
            }
        }

        @Override
        public void onChangeTrackPosition(long currentPosition) {
            if (isServiceBound) {
                int position;
                mCurrentTrackDuration = mCurrentItem.getDuration();
                mCurrentTrackPosition = currentPosition;

                position = (int) (currentPosition * 100 / mCurrentTrackDuration);

                String stringPosition = Utils.parseDurationToDate(currentPosition);
                mView.changeSeekBarPosition(position);
                mView.changeTrackPositionTextView(stringPosition);
            }
        }

        @Override
        public void onTrackFromQueue(int position) {

        }
    };

    private ServiceConnection mMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            mMusicService = binder.getService();

            isServiceBound = true;

            if (mMusicService.isTrackListExist() && mMusicService.getCurrentPosition() != -1) {
                pullDataFromService();
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
        bindServiceIfExist();
    }

    @Override
    public void onPlayPauseTrack() {
        if (isServiceBound) {
            if (isPlaying) {
                mMusicService.pauseTrack();
            } else {
                mMusicService.resumeTrack();
            }
        }
    }

    @Override
    public void onNextTrack() {
        if (isServiceBound) {
            mMusicService.nextTrack();
        }
    }

    @Override
    public void onPreviousTrack() {
        if (isServiceBound) {
            mMusicService.previousTrack();
        }
    }

    @Override
    public void onSeekTrackTo(int position) {
        if (isServiceBound) {
            mCurrentTrackDuration = mCurrentItem.getDuration();
            int seekPosition = (int) (mCurrentTrackDuration / 100 * position);
            if (Math.abs(seekPosition - mCurrentTrackPosition) >= 5000) {
                mMusicService.seekTrackTo(seekPosition);
                mView.changeTrackPositionTextView(Utils.parseDurationToDate(seekPosition));
            }
        }
    }

    @Override
    public void onLoopTrack() {
        if (isServiceBound) {
            isLooping = !isLooping;
            mMusicService.setLooping(isLooping);
            mView.showLooping(isLooping);
        }
    }

    @Override
    public void onPause() {
        if (isServiceBound) {
            mContext.unbindService(mMusicServiceConnection);
            isServiceBound = false;
        }
        mContext.unregisterReceiver(mPlayerReceiver);
    }

    @Override
    public void onResume() {
        bindServiceIfExist();
        IntentFilter filter = new IntentFilter(AudioPlayerReceiver.BROADCAST_ACTION);
        mContext.registerReceiver(mPlayerReceiver, filter);
    }

    private void pullDataFromService() {
        mCurrentItem = mMusicService.getCurrentItem();
        mView.showTrackInfo(mCurrentItem);
        isLooping = mMusicService.isLooping();
        isPlaying = mMusicService.isPlaying();
        mView.onUpdatePlayPauseButton(isPlaying);
        mView.showLooping(isLooping);
    }

    private void bindServiceIfExist() {
        if (Utils.isServiceRunning(MusicService.class.getName(), mContext) && !isServiceBound) {
            mContext.bindService(mIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
