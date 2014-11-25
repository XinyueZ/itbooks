package com.itbooks.app.fragments;

import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.itbooks.R;
import com.itbooks.adapters.BookmarkListAdapter;
import com.itbooks.bus.DeleteBookmarkEvent;
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
public final class BookmarkListFragment extends BaseFragment implements OnRefreshListener {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_bookmark_list;

	private SwipeRefreshLayout mRefreshLayout;
	private RecyclerView mBookmarksRv;

	private BookmarkListAdapter mAdp;

	private View mEmptyV;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.itbooks.bus.DeleteBookmarkEvent}.
	 *
	 * @param e
	 * 		Event {@link}.
	 */
	public void onEvent(DeleteBookmarkEvent e) {
		DSBookmark bookmark = e.getBookmark();
		deleteBookmark(bookmark);
	}



	//------------------------------------------------
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setErrorHandlerAvailable(false);
		mBookmarksRv = (RecyclerView) view.findViewById(R.id.bookmarks_rv);
		StaggeredGridLayoutManager llmgr = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

		mBookmarksRv.setLayoutManager(llmgr);

		mRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.content_srl);
		mRefreshLayout.setColorSchemeResources(R.color.green_1, R.color.green_2, R.color.green_3, R.color.green_4);
		mRefreshLayout.setOnRefreshListener(this);

		mEmptyV = view.findViewById(R.id.empty_ll);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadBookmarks();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}

	@Override
	public void onRefresh() {
		loadBookmarks();
	}

	private void loadBookmarks(){
		new ParallelTask<Void, LongSparseArray<DSBookmark>, LongSparseArray<DSBookmark>>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mRefreshLayout.setRefreshing(true);
			}

			@Override
			protected LongSparseArray<DSBookmark> doInBackground(Void... params) {
				return DB.getInstance(getActivity().getApplication()).getBookmarks(Sort.ASC, null);
			}

			@Override
			protected void onPostExecute(LongSparseArray<DSBookmark> bookmarks) {
				super.onPostExecute(bookmarks);
				if (mAdp == null) {
					mBookmarksRv.setAdapter(mAdp = new BookmarkListAdapter(bookmarks));
				} else {
					mAdp.setBookmarkList(bookmarks);
					mAdp.notifyDataSetChanged();
				}
				mRefreshLayout.setRefreshing(false);
				mEmptyV.setVisibility(bookmarks.size() <=0 ? View.VISIBLE : View.GONE);
			}
		}.executeParallel();
	}

	private void deleteBookmark(DSBookmark bookmark) {
		new ParallelTask<DSBookmark, LongSparseArray<DSBookmark>, LongSparseArray<DSBookmark>>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mRefreshLayout.setRefreshing(true);
			}

			@Override
			protected LongSparseArray<DSBookmark> doInBackground(DSBookmark... params) {
				DSBookmark bookmark = params[0];
				DB db = DB.getInstance(getActivity().getApplication());
				db.removeBookmark(bookmark);
				return db.getBookmarks(Sort.ASC, null);
			}

			@Override
			protected void onPostExecute(LongSparseArray<DSBookmark> bookmarks) {
				super.onPostExecute(bookmarks);
				mAdp.setBookmarkList(bookmarks);
				mAdp.notifyDataSetChanged();
				mRefreshLayout.setRefreshing(false);
				mEmptyV.setVisibility(bookmarks.size() <=0 ? View.VISIBLE : View.GONE);
			}
		}.executeParallel(bookmark);
	}

}
