package com.itbooks.app.fragments;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.chopping.utils.Utils;
import com.itbooks.R;
import com.itbooks.app.App;
import com.itbooks.app.adapters.HistoryAdapter;
import com.itbooks.bus.DownloadCompleteEvent;
import com.itbooks.bus.DownloadCopyEvent;
import com.itbooks.bus.DownloadDeleteEvent;
import com.itbooks.bus.DownloadMoveEvent;
import com.itbooks.bus.DownloadMovedEvent;
import com.itbooks.db.DB;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;

import org.apache.commons.io.FileUtils;

import de.greenrobot.event.EventBus;

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
	 * Handler for {@link DownloadMovedEvent}.
	 *
	 * @param e
	 * 		Event {@link DownloadMovedEvent}.
	 */
	public void onEvent(DownloadMovedEvent e) {
		getLoaderManager().initLoader(4, null, this).forceLoad();
		EventBus.getDefault().removeAllStickyEvents();
	}

	/**
	 * Handler for {@link com.itbooks.bus.DownloadCompleteEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadCompleteEvent}.
	 */
	public void onEventMainThread(DownloadCompleteEvent e) {
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}


	/**
	 * Handler for {@link DownloadDeleteEvent}.
	 *
	 * @param e
	 * 		Event {@link DownloadDeleteEvent}.
	 */
	public void onEvent(final DownloadDeleteEvent e) {
		File from = new File(App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
				e.getDownload().getTargetName());
		if (from.exists()) {
			AsyncTaskCompat.executeParallel(new AsyncTask<File, Void, IOException>() {
				@Override
				protected IOException doInBackground(File... params) {
					try {
						FileUtils.forceDelete(params[0]);
						DB.getInstance(App.Instance).deleteDownload(e.getDownload().getDownloadId());
						return null;
					} catch (IOException e1) {
						return e1;
					}
				}

				@Override
				protected void onPostExecute(IOException e) {
					super.onPostExecute(e);
					if (e == null) {
						Utils.showLongToast(App.Instance, R.string.msg_file_deleted);
						getLoaderManager().initLoader(2, null, HistoryFragment.this).forceLoad();
					} else {
						Utils.showLongToast(App.Instance, R.string.lbl_status_failed);
					}
				}
			}, from);
		} else {
			Utils.showLongToast(App.Instance, R.string.msg_file_can_be_found_to_delete);
		}
	}

	/**
	 * Handler for {@link com.itbooks.bus.DownloadCopyEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadCopyEvent}.
	 */

	public void onEvent(DownloadCopyEvent e) {
		showDirChooser(DownloadDirChooserDialogFragment.COPY, e.getDownload());
	}

	/**
	 * Handler for {@link com.itbooks.bus.DownloadMoveEvent}.
	 *
	 * @param e
	 * 		Event {@link com.itbooks.bus.DownloadMoveEvent}.
	 */
	public void onEvent(DownloadMoveEvent e) {
		showDirChooser(DownloadDirChooserDialogFragment.MOVE, e.getDownload());
	}


	//------------------------------------------------


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


	/**
	 * Open a dialog to choose directory.
	 */
	private void showDirChooser(int type, Download download) {
		DownloadDirChooserDialogFragment directoryChooserFragment = DownloadDirChooserDialogFragment.newInstance(
				App.Instance, type, download, App.Instance.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		directoryChooserFragment.show(transaction, "RDC");
	}


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
}
