package com.oleg.hubal.thebestplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.oleg.hubal.thebestplayer.model.TrackItem;

import java.io.IOException;
import java.util.List;

/**
 * Created by User on 23.11.2016.
 */

public class MusicService extends Service {

    private static final int NOTIFICATION_ID = 1121;

    private final IBinder mMusicBind = new MusicBinder();

    private Notification.Builder mNotificationBuilder;

    private MediaPlayer mMediaPlayer;
    private int mCurrentPosition = 0;
    private boolean isPlaying = false;

    private List<TrackItem> mTrackItems;


//    Listeners

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            isPlaying = true;
            updateNotification();
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

        }
    };

//    Listeners end

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);

        createNotification();
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    public void playTrackByPosition(int position) {
        mCurrentPosition = position;
        playTrack();
    }

    private void playTrack() {
        String path = mTrackItems.get(mCurrentPosition).getPath();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotification() {
        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Track title")
                .setContentText("Artist - album")
                .setStyle(new Notification.MediaStyle());
    }

    private void updateNotification() {
        String artist = mTrackItems.get(mCurrentPosition).getArtist();
        String title = mTrackItems.get(mCurrentPosition).getTitle();

        mNotificationBuilder.setContentText(artist)
                .setContentTitle(title);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    public void setTrackItems(List<TrackItem> trackItems) {
        mTrackItems = trackItems;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public List<TrackItem> getTrackItems() {
        return mTrackItems;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isTrackListExist() {
        return (mTrackItems != null);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
