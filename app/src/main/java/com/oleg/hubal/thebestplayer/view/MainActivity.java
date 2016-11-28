package com.oleg.hubal.thebestplayer.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.oleg.hubal.thebestplayer.R;
import com.oleg.hubal.thebestplayer.view.audioplayer.AudioPlayerFragment;
import com.oleg.hubal.thebestplayer.view.tracklist.TrackListFragment;

public class MainActivity extends AppCompatActivity{

    public static String[] PERMISSION_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final int REQUEST_STORAGE = 0;
    private LinearLayout mRootContainer;

    private TrackListFragment mTrackListFragment;
    private AudioPlayerFragment mAudioPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        launchAudioPlayerFragment();

        if (savedInstanceState == null) {
            checkPermissionAndLoadMedia();
        }
    }

    private void launchAudioPlayerFragment() {
        mAudioPlayerFragment = AudioPlayerFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_audio_player, mAudioPlayerFragment)
                .commit();
    }

    private void launchTrackListFragment() {
        mTrackListFragment = TrackListFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_player_track_list, mTrackListFragment)
                .commitAllowingStateLoss();
    }

    private void checkPermissionAndLoadMedia() {
        mRootContainer = (LinearLayout) findViewById(R.id.activity_main);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestExternalStoragePermission();
        } else {
            launchTrackListFragment();
        }
    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Snackbar.make(mRootContainer, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(MainActivity.this, PERMISSION_STORAGE, REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_STORAGE, REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mRootContainer, R.string.permission_available_storage,
                        Snackbar.LENGTH_SHORT).show();
                launchTrackListFragment();
            } else {
                Snackbar.make(mRootContainer, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
