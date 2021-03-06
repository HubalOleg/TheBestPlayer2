package com.oleg.hubal.thebestplayer.view.tracklist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

    private LinearLayoutManager mLayoutManager;

    private Spinner mSortSpinner;

//    LISTENERS

    private TrackListAdapter.OnTrackItemClickListener mOnTrackItemClickListener = new TrackListAdapter.OnTrackItemClickListener() {
        @Override
        public void onTrackClicked(int position) {
            mPresenter.onTrackSelected(position);
        }

        @Override
        public void onQueueClicked(int position) {
            mPresenter.onQueueSelected(position);
        }
    };

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            mPresenter.onSortItems(adapterView.getItemAtPosition(i).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

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
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.onResume();
        }
    }

    private void init() {
        mPresenter = new TrackListPresenter(getContext(), TrackListFragment.this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracklist, container, false);

        mSortSpinner = (Spinner) view.findViewById(R.id.spr_sort_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_sort_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(adapter);
        mSortSpinner.setOnItemSelectedListener(mOnItemSelectedListener);

        RecyclerView trackRecyclerView = (RecyclerView) view.findViewById(R.id.rv_track_list_recycler);
        trackRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getContext());
        trackRecyclerView.setLayoutManager(mLayoutManager);

        mTrackListAdapter = new TrackListAdapter(getContext(), mOnTrackItemClickListener);
        trackRecyclerView.setAdapter(mTrackListAdapter);

        mPresenter.onFillTrackList();
        return view;
    }

    @Override
    public void setSelectedItem(int position) {
        mTrackListAdapter.setTrackSelected(position);
    }

    @Override
    public void setItemQueue(int itemPosition) {
        mTrackListAdapter.setQueueSelected(itemPosition);
    }

    @Override
    public void setTrackItems(List<TrackItem> trackItems) {
        mTrackListAdapter.setData(trackItems);
    }

    @Override
    public void launchTrackListLoader() {
        getLoaderManager().initLoader(0, null, mPresenter.getTrackListLoader());
    }

    @Override
    public void unSelectAll() {
        mTrackListAdapter.unSelectItems();
    }

    @Override
    public void showTrackList(List<TrackItem> trackList) {
        mTrackListAdapter.setData(trackList);
    }

    @Override
    public void scrollListToPosition(int position) {
        mLayoutManager.scrollToPositionWithOffset(position, mLayoutManager.getHeight() / 2);
        mTrackListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showSortedList() {
        mTrackListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onStop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.onPause();
        }
    }
}
