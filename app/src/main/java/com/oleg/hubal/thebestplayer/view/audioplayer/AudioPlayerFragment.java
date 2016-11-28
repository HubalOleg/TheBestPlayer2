package com.oleg.hubal.thebestplayer.view.audioplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.oleg.hubal.thebestplayer.R;
import com.oleg.hubal.thebestplayer.presenter.audioplayer.AudioPlayerPresenter;
import com.oleg.hubal.thebestplayer.presenter.audioplayer.AudioPlayerPresenterContract;

/**
 * Created by User on 23.11.2016.
 */

public class AudioPlayerFragment extends Fragment implements AudioPlayerViewContract {

    private AudioPlayerPresenterContract mPresenter;

    private SeekBar mTrackPositionSeekBack;
    private TextView mTrackInfoTextView;
    private TextView mTrackPositionTextView;
    private TextView mTrackDurationTextView;
    private ImageButton mPlayPauseImageButton;
    private ImageButton mTrackLoopImageButton;
    private ImageButton mNextTrackImageButton;

    public static AudioPlayerFragment newInstance() {
        return new AudioPlayerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        mPresenter = new AudioPlayerPresenter(getContext(), AudioPlayerFragment.this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audioplayer, container, false);

        initViews(view);
        setClickListener();

        return view;
    }

    private void initViews(View view) {
        mTrackPositionSeekBack = (SeekBar) view.findViewById(R.id.sb_track_position_bar);
        mTrackInfoTextView = (TextView) view.findViewById(R.id.tv_track_info);
        mTrackDurationTextView = (TextView) view.findViewById(R.id.tv_track_duration);
        mTrackPositionTextView = (TextView) view.findViewById(R.id.tv_track_current_position);
        mPlayPauseImageButton = (ImageButton) view.findViewById(R.id.ibtn_play_pause);
        mTrackLoopImageButton = (ImageButton) view.findViewById(R.id.ibtn_track_loop);
        mNextTrackImageButton = (ImageButton) view.findViewById(R.id.ibtn_next_track);
    }

    private void setClickListener() {
        mPlayPauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onPlayPauseAction();
            }
        });
    }



    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onStop();
        }
    }
}
