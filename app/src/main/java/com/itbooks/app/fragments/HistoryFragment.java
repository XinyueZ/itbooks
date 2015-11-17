package com.itbooks.app.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
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
import com.itbooks.bus.LoginRequestEvent;
import com.itbooks.db.DB;
import com.itbooks.net.SyncService;
import com.itbooks.net.download.Download;
import com.itbooks.utils.Prefs;

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
	private Toolbar mToolbar;

	/**
	 * Handler filter end sync.
	 */
	private IntentFilter mSyncEndHandlerFilter = new IntentFilter(SyncService.ACTION_SYNC_END);
	/**
	 * Handler   end sync.
	 */
	private BroadcastReceiver mSyncEndHandler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			getLoaderManager().initLoader(2, null, HistoryFragment.this).forceLoad();
		}
	};
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


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
			AsyncTaskCompat.executeParallel(new AsyncTask<Download, Void, IOException>() {
				@Override
				protected IOException doInBackground(Download... params) {
					List<Download> downloadsList = new ArrayList<>();
					downloadsList.add(params[0]);
					SyncService.startSyncDel(App.Instance, downloadsList);
					return null;
				}
			}, e.getDownload());
		} else {
			Utils.showLongToast(App.Instance, R.string.msg_file_delete_failed);
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


	//------------------------------------------------


	@Override
	public Loader<List<Download>> onCreateLoader(int id, Bundle args) {
		return getAllDownloads();
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
			mToolbar.setVisibility(View.VISIBLE);
			mEmptyV.setVisibility(View.INVISIBLE);
		} else {
			mHistoryRv.setVisibility(View.INVISIBLE);
			mToolbar.setVisibility(View.INVISIBLE);
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


	@NonNull
	public AsyncTaskLoader<List<Download>> getAllDownloads() {
		return new AsyncTaskLoader<List<Download>>(App.Instance) {
			@Override
			public List<Download> loadInBackground() {
				return DB.getInstance(App.Instance).getDownloads();
			}
		};
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LocalBroadcastManager.getInstance(App.Instance).registerReceiver(mSyncEndHandler, mSyncEndHandlerFilter);
		mHistoryRv = (RecyclerView) view.findViewById(R.id.history_rv);
		mHistoryRv.setLayoutManager(new LinearLayoutManager(getActivity()));
		mHistoryRv.setAdapter(mHistoryAdapter = new HistoryAdapter(null));
		mEmptyV = view.findViewById(R.id.empty_ll);
		mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
		mToolbar.inflateMenu(R.menu.history_list_menu);
		Menu menu = mToolbar.getMenu();
		menu.findItem(R.id.action_sync).setOnMenuItemClickListener(new OnMenuItemClickListener() {
																	   @Override
																	   public boolean onMenuItemClick(MenuItem item) {
																		   if (!TextUtils.isEmpty(Prefs.getInstance(
																				   App.Instance).getGoogleId())) {
																			   SyncService.startSync(App.Instance);
																		   } else {
																			   EventBus.getDefault().post(
																					   new LoginRequestEvent());
																		   }
																		   return true;
																	   }
																   }

		);
		menu.findItem(R.id.action_delete_all).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				getLoaderManager().initLoader(1, null, new LoaderCallbacks<List<Download>>() {
					@Override
					public Loader<List<Download>> onCreateLoader(int id, Bundle args) {
						return getAllDownloads();
					}

					@Override
					public void onLoadFinished(Loader<List<Download>> loader, List<Download> downloadsList) {
						SyncService.startSyncDel(App.Instance, downloadsList);
					}

					@Override
					public void onLoaderReset(Loader<List<Download>> loader) {

					}
				}).forceLoad();
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(1, null, this).forceLoad();
	}

	@Override
	public void onDestroyView() {
		LocalBroadcastManager.getInstance(App.Instance).unregisterReceiver(mSyncEndHandler);
		super.onDestroyView();
	}
}
