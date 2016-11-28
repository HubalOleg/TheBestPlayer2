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
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.view.MainActivity;

import java.io.IOException;
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

    public static final String BROADCAST_ACTION = "com.oleg.hubal.thebestplayer.ACTION_BROADCAST";
    public static final String PARAM_ACTION = "action";

    private static final int NOTIFICATION_ID = 1121;

    private final IBinder mMusicBind = new MusicBinder();

    private NotificationCompat.Builder mNotificationBuilder;

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private int mCurrentPosition = -1;
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
            nextTrack();
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
                sendActionBroadcast(ACTION_PLAY);
                mController.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                sendActionBroadcast(ACTION_PAUSE);
                mController.getTransportControls().pause();
                break;
            case ACTION_NEXT:
                sendActionBroadcast(ACTION_NEXT);
                mController.getTransportControls().skipToNext();
                break;
            case ACTION_PREVIOUS:
                sendActionBroadcast(ACTION_PREVIOUS);
                mController.getTransportControls().skipToPrevious();
                break;
            case ACTION_STOP:
                sendActionBroadcast(ACTION_STOP);
                mController.getTransportControls().stop();
                break;
        }
    }

    private void sendActionBroadcast(String action) {
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra(PARAM_ACTION, action);
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

                Intent intent = new Intent(getApplicationContext(), MusicService.class);
                stopService(intent);

                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
            }
        });
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

    public void playTrackByPosition(int position) {
        mCurrentPosition = position;
        playTrack();
    }

    private void playTrack() {
        Log.d(TAG, "playTrack: " + mCurrentPosition);
        String path = mTrackItems.get(mCurrentPosition).getPath();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextTrack() {
        mCurrentPosition++;
        if (mCurrentPosition >= mTrackItems.size()) mCurrentPosition = 0;
        playTrack();
    }

    private void previousTrack() {
        mCurrentPosition--;
        if (mCurrentPosition < 0) mCurrentPosition = mTrackItems.size() - 1;
        playTrack();
    }

    private void pauseTrack() {
        if (mMediaPlayer != null && isPlaying) {
            mMediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void resumeTrack() {
        if (mMediaPlayer != null && !isPlaying) {
            mMediaPlayer.start();
            isPlaying = true;
        }
    }

    private void stopMedia() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        isPlaying = false;


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

    public List<TrackItem> getTrackItems() {
        return mTrackItems;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isTrackListExist() {
        return (mTrackItems != null);
    }

    public interface OnPlayerActionListener {
        void play();
        void pause();
        void next();
        void previous();
        void stop();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
