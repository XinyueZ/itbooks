package com.itbooks.app.fragments;

import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.itbooks.R;
import com.itbooks.adapters.BookmarkListAdapter;
import com.itbooks.data.DSBookmark;
import com.itbooks.db.DB;
import com.itbooks.db.DB.Sort;
import com.itbooks.utils.ParallelTask;
import com.itbooks.utils.Prefs;

/**
 * Show list of all bookmarks.
 *
 * @author Xinyue Zhao
 */
public final class BookmarkListFragment extends BaseFragment {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_bookmark_list;

	private RecyclerView mBookmarksRv;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setErrorHandlerAvailable(false);
		mBookmarksRv = (RecyclerView) view.findViewById(R.id.bookmarks_rv);
		LinearLayoutManager llmgr = new LinearLayoutManager(getActivity());
		mBookmarksRv.setLayoutManager(llmgr);

		new ParallelTask<Void, LongSparseArray<DSBookmark>,  LongSparseArray<DSBookmark>>() {
			@Override
			protected LongSparseArray<DSBookmark> doInBackground(Void... params) {
				return DB.getInstance(getActivity().getApplication()).getBookmarks(Sort.ASC, null);
			}

			@Override
			protected void onPostExecute(LongSparseArray<DSBookmark> bookmarks) {
				super.onPostExecute(bookmarks);
				mBookmarksRv.setAdapter(new BookmarkListAdapter(bookmarks));
			}
		}.executeParallel();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}
}
