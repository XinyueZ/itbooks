package com.itbooks.app.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.itbooks.app.App;
import com.itbooks.R;
import com.itbooks.adapters.HistoryAdapter;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.db.DB;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;

/**
 * Show all download-history.
 *
 * @author Xinyue Zhao
 */
public final class HistoryFragment extends BaseFragment implements LoaderCallbacks<List<Download>> {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_history;
	private RecyclerView mHistoryRv;
	private HistoryAdapter mHistoryAdapter;
	private View mEmptyV;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link com.itbooks.bus.DownloadCompleteEvent}.
	 * @param e Event {@link com.itbooks.bus.DownloadCompleteEvent}.
	 */
	public void onEventMainThread(DownloadCompleteEvent e) {
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}

	//------------------------------------------------
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mHistoryRv = (RecyclerView) view.findViewById(R.id.history_rv);
		mHistoryRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		mHistoryRv.setAdapter(mHistoryAdapter = new HistoryAdapter(null));
		mEmptyV = view.findViewById(R.id.empty_ll);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(1, null, this).forceLoad();
	}

	@Override
	public Loader<List<Download>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<List<Download>>(App.Instance) {
			@Override
			public List<Download> loadInBackground() {
				return DB.getInstance(App.Instance).getDownloads();
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<List<Download>> loader, List<Download> data) {
		showData(data);
	}


	@Override
	public void onLoaderReset(Loader<List<Download>> loader) {

	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}

	/**
	 * Show all history of downloads.
	 *
	 * @param data
	 * 		List of all downloads.
	 */
	private void showData(List<Download> data) {
		if (data.size() > 0) {
			mHistoryAdapter.setData(data);
			mHistoryAdapter.notifyDataSetChanged();
			mHistoryRv.setVisibility(View.VISIBLE);
			mEmptyV.setVisibility(View.GONE);
		} else {
			mHistoryRv.setVisibility(View.GONE);
			mEmptyV.setVisibility(View.VISIBLE);
		}
	}

}
