package com.oleg.hubal.thebestplayer.view.tracklist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oleg.hubal.thebestplayer.R;
import com.oleg.hubal.thebestplayer.adapter.TrackListAdapter;
import com.oleg.hubal.thebestplayer.model.TrackItem;
import com.oleg.hubal.thebestplayer.presenter.tracklist.TrackListPresenter;
import com.oleg.hubal.thebestplayer.presenter.tracklist.TrackListPresenterContract;

import java.util.List;

/**
 * Created by User on 22.11.2016.
 */

public class TrackListFragment extends Fragment implements TrackListViewContract {

    private static final String TAG = "TrackListFragment";

    private TrackListPresenterContract mPresenter;

    private TrackListAdapter mTrackListAdapter;

//    LISTENERS

    private TrackListAdapter.OnTrackItemClickListener mOnTrackItemClickListener = new TrackListAdapter.OnTrackItemClickListener() {
        @Override
        public void onTrackClicked(int position) {
            mTrackListAdapter.setTrackSelected(position);
            mPresenter.onTrackSelected(position);
        }

        @Override
        public void onQueueClicked(int position) {
            mTrackListAdapter.setQueueSelected(position);
        }
    };

//    LISTENERS END!

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.onStart();
        }
    }

    private void init() {
        mPresenter = new TrackListPresenter(getContext(), TrackListFragment.this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracklist, container, false);

        RecyclerView trackRecyclerView = (RecyclerView) view.findViewById(R.id.rv_track_list_recycler);
        trackRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        trackRecyclerView.setLayoutManager(layoutManager);

        mTrackListAdapter = new TrackListAdapter(getContext(), mOnTrackItemClickListener);
        trackRecyclerView.setAdapter(mTrackListAdapter);

        launchTrackListLoader();
        return view;
    }

    @Override
    public void setSelectedItem(int position) {
        mTrackListAdapter.setTrackSelected(position);
    }

    private void launchTrackListLoader() {
        getLoaderManager().initLoader(0, null, mPresenter.getTrackListLoader());
    }

    @Override
    public void showTrackList(List<TrackItem> trackList) {
        mTrackListAdapter.setData(trackList);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onStop();
        }
    }
}
