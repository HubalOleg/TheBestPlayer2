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

    private static final String TAG = "MusicService";

    public static final String ACTION_PLAY = "com.oleg.hubal.thebestplayer.INTENT_PLAY";
    public static final String ACTION_PAUSE = "com.oleg.hubal.thebestplayer.INTENT_PAUSE";
    public static final String ACTION_NEXT = "com.oleg.hubal.thebestplayer.INTENT_NEXT";
    public static final String ACTION_PREVIOUS = "com.oleg.hubal.thebestplayer.INTENT_PLAY_PAUSE";
    public static final String ACTION_STOP = "com.oleg.hubal.thebestplayer.INTENT_STOP";
    public static final String ACTION_CHANGE_TRACK = "com.oleg.hubal.thebestplayer.INTENT_CHANGE_TRACK";
    public static final String ACTION_CHANGE_CURRENT_POSITION = "com.oleg.hubal.thebestplayer.INTENT_CURRENT_POSITION";
    public static final String ACTION_QUEUE = "com.oleg.hubal.thebestplayer.INTENT_PLAY_FROM_QUEUE";

    private static final int NOTIFICATION_ID = 1121;

    private final IBinder mMusicBind = new MusicBinder();

    private NotificationCompat.Builder mNotificationBuilder;

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private int mCurrentPosition = -1;
    private boolean isPlaying = false;

    private boolean isLooping = false;

    private List<TrackItem> mTrackItems;
    private List<Integer> mQueueList = new ArrayList();

    Handler mSeekHandler = new Handler();


//    Listeners

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            stopProgressUpdate();
            mediaPlayer.start();
            sendActionBroadcast(ACTION_CHANGE_TRACK);
            isPlaying = true;
            updateNotification();
            startProgressUpdate();
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

    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(AudioPlayerReceiver.BROADCAST_ACTION);
            intent.putExtra(AudioPlayerReceiver.PARAM_ACTION, ACTION_CHANGE_CURRENT_POSITION);
            intent.putExtra(AudioPlayerReceiver.PARAM_CURRENT_POSITION, mMediaPlayer.getCurrentPosition());
            sendBroadcast(intent);
            mSeekHandler.postDelayed(mUpdateRunnable, 500);
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
        mSession.release();
        return false;
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

        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                mController.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                mController.getTransportControls().pause();
                break;
            case ACTION_NEXT:
                mController.getTransportControls().skipToNext();
                break;
            case ACTION_PREVIOUS:
                mController.getTransportControls().skipToPrevious();
                break;
            case ACTION_STOP:
                mController.getTransportControls().stop();
                break;
        }
    }

    private void sendActionBroadcast(String action) {
        Intent broadcastIntent = new Intent(AudioPlayerReceiver.BROADCAST_ACTION);
        broadcastIntent.putExtra(AudioPlayerReceiver.PARAM_ACTION, action);
        sendBroadcast(broadcastIntent);
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
                stopProgressUpdate();

                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                stopService(intent);

                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
            }
        });
    }

    private void startProgressUpdate() {
        mSeekHandler.post(mUpdateRunnable);
    }

    private void stopProgressUpdate() {
        mSeekHandler.removeCallbacks(mUpdateRunnable);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
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
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS))
                .addAction(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY))
                .addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE))
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT))
                .addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", ACTION_STOP))
                .setStyle(new NotificationCompat.MediaStyle().setShowCancelButton(true));
    }

    public TrackItem getCurrentItem() {
        return mTrackItems.get(mCurrentPosition);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
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

    public void nextTrack() {
        if (mCurrentPosition != -1) {

            unSelectPrevious();

            if (mQueueList.size() == 0) {
                mCurrentPosition++;
                sendActionBroadcast(ACTION_NEXT);
                if (mCurrentPosition >= mTrackItems.size()) mCurrentPosition = 0;
                playTrack();
            } else {
                playTrackFromQueue();
            }
        }
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
        Intent queueIntent = new Intent(AudioPlayerReceiver.BROADCAST_ACTION);
        queueIntent.putExtra(AudioPlayerReceiver.PARAM_ACTION, ACTION_QUEUE);
        queueIntent.putExtra(AudioPlayerReceiver.PARAM_QUEUE_POSITION, mCurrentPosition);
        sendBroadcast(queueIntent);
    }

    public void previousTrack() {
        if (mCurrentPosition != -1) {
            unSelectPrevious();
            mCurrentPosition--;
            if (mCurrentPosition < 0) mCurrentPosition = mTrackItems.size() - 1;
            playTrack();
            sendActionBroadcast(ACTION_PREVIOUS);
        }
    }

    private void unSelectPrevious() {
        mTrackItems.get(mCurrentPosition).setSelected(false);
    }

    public void pauseTrack() {
        if (mMediaPlayer != null && isPlaying) {
            stopProgressUpdate();
            mMediaPlayer.pause();
            isPlaying = false;
            sendActionBroadcast(ACTION_PAUSE);
        }
    }

    public void resumeTrack() {
        if (mMediaPlayer != null && !isPlaying) {
            startProgressUpdate();
            mMediaPlayer.start();
            isPlaying = true;
            sendActionBroadcast(ACTION_PLAY);
        }
    }

    public void seekTrackTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void stopMedia() {
        stopProgressUpdate();
        mCurrentPosition = -1;
        mMediaPlayer.stop();
        isPlaying = false;
        sendActionBroadcast(ACTION_STOP);
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

    public void setTrackItems(List<TrackItem> trackItems) {
        mTrackItems = trackItems;
        mCurrentPosition = -1;
    }

    public List<TrackItem> getTrackItems() {
        return mTrackItems;
    }

    public void setQueueList(List<Integer> queueList) {
        mQueueList = queueList;
    }

    public List<Integer> getQueueList() {
        return mQueueList;
    }

    public boolean isQueueListExist() {
        return (mQueueList.size() != 0);
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
