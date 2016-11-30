package com.oleg.hubal.thebestplayer.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.oleg.hubal.thebestplayer.R;
import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.view.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.NotificationCompat.Action;
import static android.support.v4.app.NotificationCompat.CATEGORY_SERVICE;

/**
 * Created by User on 23.11.2016.
 */

public class MusicService extends Service {
    public static final String ACTION_PLAY = "com.oleg.hubal.thebestplayer.INTENT_PLAY";
    public static final String ACTION_PAUSE = "com.oleg.hubal.thebestplayer.INTENT_PAUSE";
    public static final String ACTION_NEXT = "com.oleg.hubal.thebestplayer.INTENT_NEXT";
    public static final String ACTION_PREVIOUS = "com.oleg.hubal.thebestplayer.INTENT_PLAY_PAUSE";
    public static final String ACTION_STOP = "com.oleg.hubal.thebestplayer.INTENT_STOP";
    public static final String ACTION_CHANGE_TRACK = "com.oleg.hubal.thebestplayer.INTENT_CHANGE_TRACK";
    public static final String ACTION_CHANGE_CURRENT_POSITION = "com.oleg.hubal.thebestplayer.INTENT_CURRENT_POSITION";
    public static final String ACTION_QUEUE = "com.oleg.hubal.thebestplayer.INTENT_PLAY_FROM_QUEUE";

    private static final int NOTIFICATION_ID = 1121;

    private NotificationCompat.Builder mNotificationBuilder;

    private final IBinder mMusicBind = new MusicBinder();

    private HeadphoneReceiver mHeadphoneReceiver;

    private List<TrackItem> mTrackItems;
    private List<Integer> mQueueList = new ArrayList();

    private MediaSessionManager mManager;
    private MediaController mController;
    private MediaPlayer mMediaPlayer;
    private MediaSession mSession;

    private boolean isPlaying = false;
    private boolean isLooping = false;
    private int mCurrentPosition = -1;

    Handler mSeekHandler = new Handler();

//    Listeners

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            stopProgressUpdate();
            mediaPlayer.start();
            sendActionBroadcast(ACTION_CHANGE_TRACK);
            isPlaying = true;
            createNotification();
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
                if (mCurrentPosition != -1) {
                    playTrack();
                }
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

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }

    public boolean isLooping() {
        return isLooping;
    }
    public TrackItem getCurrentItem() {
        return mTrackItems.get(mCurrentPosition);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }



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

        mHeadphoneReceiver = new HeadphoneReceiver();

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadphoneReceiver, filter);
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

        Bitmap bitmap = getAlbumArt();

        String title = "";
        String artist = "";

        if (mCurrentPosition != -1) {
            artist = mTrackItems.get(mCurrentPosition).getArtist();
            title = mTrackItems.get(mCurrentPosition).getTitle();
        }

        Action action;

        if (isPlaying) {
            action = generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE);
        } else {
            action = generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY);
        }

        mNotificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false)
                .setCategory(CATEGORY_SERVICE)
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS))
                .addAction(action)
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT))
                .addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", ACTION_STOP))
                .setStyle(new NotificationCompat.MediaStyle());

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
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
            createNotification();
            sendActionBroadcast(ACTION_PAUSE);
        }
    }

    public void resumeTrack() {
        if (mMediaPlayer != null && !isPlaying) {
            startProgressUpdate();
            mMediaPlayer.start();
            isPlaying = true;
            createNotification();
            sendActionBroadcast(ACTION_PLAY);
        }
    }

    public void seekTrackTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    public void stopMedia() {
        stopProgressUpdate();
        mCurrentPosition = -1;
        mMediaPlayer.stop();
        isPlaying = false;
        createNotification();
        sendActionBroadcast(ACTION_STOP);
    }

    private void startProgressUpdate() {
        mSeekHandler.post(mUpdateRunnable);
    }

    private void stopProgressUpdate() {
        mSeekHandler.removeCallbacks(mUpdateRunnable);
    }

    private Bitmap getAlbumArt() {
        byte[] data = null;
        if (mCurrentPosition != -1) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(mTrackItems.get(mCurrentPosition).getPath());
            data = mmr.getEmbeddedPicture();
        }
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        }
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private class HeadphoneReceiver extends BroadcastReceiver {
        private static final int STATE_UNPLUGGED = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.getIntExtra("state", -1) == STATE_UNPLUGGED) {
                    pauseTrack();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHeadphoneReceiver);
    }
}
