package com.oleg.hubal.thebestplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.view.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 23.11.2016.
 */

public class MusicService extends Service {

    private final IBinder mMusicBind = new MusicBinder();

    private List<Integer> mQueueList = new ArrayList();
    private List<TrackItem> mTrackItems;

    private NotificationCompat.Builder mNotificationBuilder;
    private static final int NOTIFICATION_ID = 0;

    private MediaSessionManager mManager;
    private MediaController mController;
    private MediaPlayer mMediaPlayer;
    private MediaSession mSession;

    private int mCurrentPosition = -1;
    private boolean isPlaying = false;
    private boolean isLooping = false;

    Handler mUpdateProgressHandler = new Handler();

//    Listeners
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            stopUpdateTrackProgress();
            mediaPlayer.start();
            isPlaying = true;
            updateNotification();
            startUpdateTrackProgress();
            sendActionBroadcast(ServiceConstants.ACTION_CHANGE_TRACK);
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
            if (isLooping) {
                playTrack();
            } else {
                nextTrack();
            }
        }
    };

    private Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(ServiceConstants.BROADCAST_ACTION);
            intent.putExtra(ServiceConstants.PARAM_ACTION, ServiceConstants.ACTION_CHANGE_CURRENT_POSITION);
            intent.putExtra(ServiceConstants.PARAM_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
            sendBroadcast(intent);
            mUpdateProgressHandler.postDelayed(mUpdateProgressRunnable, 500);
        }
    };

//    Listeners end

    public boolean isQueueListExist() {
        return (mQueueList.size() != 0);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isTrackListExist() {
        return (mTrackItems != null);
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }

    public TrackItem getCurrentItem() {
        return mTrackItems.get(mCurrentPosition);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public List<Integer> getQueueList() {
        return mQueueList;
    }

    public void setQueueList(List<Integer> queueList) {
        mQueueList = queueList;
    }

    public List<TrackItem> getTrackItems() {
        return mTrackItems;
    }

    public void setTrackItems(List<TrackItem> trackItems) {
        mTrackItems = trackItems;
        mCurrentPosition = -1;}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initMediaPlayer();
        initMediaSession();

        createNotification();
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        switch (intent.getAction()) {
            case ServiceConstants.ACTION_PLAY:
                mController.getTransportControls().play();
                break;
            case ServiceConstants.ACTION_PAUSE:
                mController.getTransportControls().pause();
                break;
            case ServiceConstants.ACTION_NEXT:
                mController.getTransportControls().skipToNext();
                break;
            case ServiceConstants.ACTION_PREVIOUS:
                mController.getTransportControls().skipToPrevious();
                break;
            case ServiceConstants.ACTION_STOP:
                mController.getTransportControls().stop();
                break;
        }
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
    }

    private void initMediaSession() {
        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback(){
            @Override
            public void onPlay() {
                super.onPlay();
                resumeTrack();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseTrack();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                nextTrack();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                previousTrack();
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                stopUpdateTrackProgress();

                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                stopService(intent);

                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
            }
        });
    }

    private void createNotification() {
        Intent notifyIntent = new Intent(getApplicationContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Track title")
                .setContentText("Artist - album")
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ServiceConstants.ACTION_PREVIOUS))
                .addAction(generateAction(android.R.drawable.ic_media_play, "Play", ServiceConstants.ACTION_PLAY))
                .addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", ServiceConstants.ACTION_PAUSE))
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ServiceConstants.ACTION_NEXT))
                .addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", ServiceConstants.ACTION_STOP))
                .setStyle(new NotificationCompat.MediaStyle().setShowCancelButton(true));
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void playTrackFromQueue() {
        mCurrentPosition = mQueueList.get(0);
        mTrackItems.get(mCurrentPosition).setQueuePosition(-1);
        mQueueList.remove(0);

        for (int i : mQueueList) {
            TrackItem queueItem = mTrackItems.get(i);
            int oldPosition = queueItem.getQueuePosition();
            queueItem.setQueuePosition(oldPosition - 1);
        }

        sendQueueBroadcast();

        playTrack();
    }

    private void sendQueueBroadcast() {
        Intent queueIntent = new Intent(ServiceConstants.BROADCAST_ACTION);
        queueIntent.putExtra(ServiceConstants.PARAM_ACTION, ServiceConstants.ACTION_QUEUE);
        queueIntent.putExtra(ServiceConstants.PARAM_QUEUE_POSITION, mCurrentPosition);
        sendBroadcast(queueIntent);
    }

    public void nextTrack() {
        if (mCurrentPosition != -1) {

            unSelectPrevious();

            if (mQueueList.size() == 0) {
                mCurrentPosition++;
                sendActionBroadcast(ServiceConstants.ACTION_NEXT);
                if (mCurrentPosition >= mTrackItems.size()) mCurrentPosition = 0;
                playTrack();
            } else {
                playTrackFromQueue();
            }
        }
    }

    public void previousTrack() {
        if (mCurrentPosition != -1) {
            unSelectPrevious();
            mCurrentPosition--;
            if (mCurrentPosition < 0) mCurrentPosition = mTrackItems.size() - 1;
            playTrack();
            sendActionBroadcast(ServiceConstants.ACTION_PREVIOUS);
        }
    }

    private void unSelectPrevious() {
        mTrackItems.get(mCurrentPosition).setSelected(false);
    }

    public void playTrackByPosition(int position) {
        if (mQueueList.size() != 0 && mQueueList.get(0) == position) {
            playTrackFromQueue();
        } else {
            mCurrentPosition = position;
            playTrack();
        }
    }

    private void playTrack() {
        mTrackItems.get(mCurrentPosition).setSelected(true);
        String path = mTrackItems.get(mCurrentPosition).getPath();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resumeTrack() {
        if (mMediaPlayer != null && !isPlaying) {
            startUpdateTrackProgress();
            mMediaPlayer.start();
            isPlaying = true;
            sendActionBroadcast(ServiceConstants.ACTION_PLAY);
        }
    }

    public void pauseTrack() {
        if (mMediaPlayer != null && isPlaying) {
            stopUpdateTrackProgress();
            mMediaPlayer.pause();
            isPlaying = false;
            sendActionBroadcast(ServiceConstants.ACTION_PAUSE);
        }
    }

    public void stopMedia() {
        stopUpdateTrackProgress();
        mCurrentPosition = -1;
        mMediaPlayer.stop();
        isPlaying = false;
        sendActionBroadcast(ServiceConstants.ACTION_STOP);
    }

    public void seekTrackToPosition(int position) {
        mMediaPlayer.seekTo(position);
    }

    private void sendActionBroadcast(String action) {
        Intent broadcastIntent = new Intent(ServiceConstants.BROADCAST_ACTION);
        broadcastIntent.putExtra(ServiceConstants.PARAM_ACTION, action);
        sendBroadcast(broadcastIntent);
    }

    public void onSearchSortAction() {
        stopMedia();
        mNotificationBuilder.setContentText("Choose song...")
                .setContentTitle("");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void updateNotification() {
        String artist = mTrackItems.get(mCurrentPosition).getArtist();
        String title = mTrackItems.get(mCurrentPosition).getTitle();

        mNotificationBuilder.setContentText(artist)
                .setContentTitle(title);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void startUpdateTrackProgress() {
        mUpdateProgressHandler.post(mUpdateProgressRunnable);
    }

    private void stopUpdateTrackProgress() {
        mUpdateProgressHandler.removeCallbacks(mUpdateProgressRunnable);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSession.release();
        return false;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
